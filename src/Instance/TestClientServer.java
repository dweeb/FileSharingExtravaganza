package Instance;

import HostBehavior.FileClient;
import HostBehavior.FileOperationException;
import Instance.OwnState;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class TestClientServer {
    public static final int PORT = 61337;
    public static void main(String[] args) throws NoSuchAlgorithmException, InterruptedException, IOException, FileOperationException {
        //
        OwnState serverState = new OwnState("/home/piotr/test2");
        OwnState clientState = new OwnState("/home/piotr/test1");
        Thread serverThread = new Thread(new Listener(serverState, PORT));
        Thread clientThread = new Thread(new FileClient("localhost", PORT, clientState));
        serverThread.start();
        clientThread.start();
        System.out.println("This is a debug message");
    }
}
