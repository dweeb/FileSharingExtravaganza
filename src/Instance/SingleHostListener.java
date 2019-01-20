package Instance;

import HostBehavior.FileSeeder;
import Instance.OwnState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class SingleHostListener implements Runnable{
    private OwnState state;
    private int port;
    private ServerSocket serverSocket;
    private boolean connected;
    private Socket socket;
    public SingleHostListener(OwnState state, int port){
        this.state = state;
        this.port = port;
        connected = false;
    }
    @Override
    public void run(){
        try(
            ServerSocket newSocket = new ServerSocket(port);
        ) {
            serverSocket = newSocket;
            socket = newSocket.accept();
            connected = true;
            new FileSeeder(state, socket).run();
        } catch (IOException e) {
            System.out.println("ServerSocket was closed");
        }
    }
    public void closeServer(){
        try {
            serverSocket.close();
            socket.close();
        } catch (IOException e) {
            // nothing
        }
    }
    public boolean isConnected(){
        return connected;
    }
}
