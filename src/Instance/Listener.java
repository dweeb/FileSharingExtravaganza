package Instance;

import HostBehavior.FileSeeder;
import Instance.OwnState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener implements Runnable{
    private OwnState state;
    private int port;
    private ServerSocket serverSocket;
    public Listener(OwnState state, int port){
        this.state = state;
        this.port = port;
    }
    public void run(){
        boolean dead = false;
        try(
            ServerSocket newSocket = new ServerSocket(port);
            //  add timeout in the future pls
        ) {
            serverSocket = newSocket;
            while(!dead){
                try{
                    Socket socket = newSocket.accept();
                    System.out.println("Socket connection accepted");
                    new Thread(new FileSeeder(state, socket)).run();
                } catch (IOException e) {
                    System.err.println("Connection interrupted");
                    e.printStackTrace();
                    dead = true;
                }
                /**
                 *  the proper logic for timeouts and deading should later be implemented
                 */
                //dead = true;
            }
        } catch (IOException e) {
            System.out.println("ServerSocket was closed");
            e.printStackTrace();
        }
    }
    public void closeServer(){
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
