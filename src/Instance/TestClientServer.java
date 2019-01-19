package Instance;

import HostBehavior.FileClient;
import HostBehavior.FileOperationException;
import Instance.OwnState;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class TestClientServer {
    public static void main(String[] args) throws NoSuchAlgorithmException, InterruptedException, IOException, FileOperationException {
        //
        OwnState serverState = new OwnState("/home/piotr/Pictures");
        OwnState clientState = new OwnState("/home/piotr/FileSharingTest");
        Thread serverThread = new Thread(new Listener(serverState));
        Thread clientThread = new Thread(new FileClient("localhost", Listener.PORT, clientState));
        serverThread.start();
        clientThread.start();
        System.out.println("This is a debug message");
    }
}
