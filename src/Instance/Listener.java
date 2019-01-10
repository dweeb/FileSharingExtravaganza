package Instance;

import HostBehavior.FileSeeder;
import HostBehavior.OwnState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class Listener {
    public static final int PORT = 61337;
    public static void main(String[] args){
        boolean dead = false;
        try(
            ServerSocket newSocket = new ServerSocket(PORT);
            //  add timeout in the future pls
        ) {
            OwnState state = new OwnState("/home/piotr/aa");    //  @TODO later move to instance-level
            while(!dead){
                try{
                    Socket socket = newSocket.accept();
                    new Thread(new FileSeeder(state, socket)).run();
                } catch (IOException e) {
                    System.err.println("IOException in the inner loop");
                    e.printStackTrace();
                    dead = true;
                }
                /**
                 *  the proper logic for timeouts and deading should later be implemented
                 */
                dead = true;
            }
        } catch (IOException e) {
            System.err.println("IOException in the outer scope");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
