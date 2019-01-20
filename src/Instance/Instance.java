package Instance;

import HostBehavior.FileClient;
import HostBehavior.FileOperationException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static Instance.Mode.*;

public class Instance {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, FileOperationException, InterruptedException {
        int ownPort = 61337;
        String peerAddress = "localhost";
        int peerPort = 61338;
        String ownDir = System.getenv("HOME");
        Mode mode = SINGLE_CLIENT;
        //  program invocation : java -jar JARNAME -p=port -a=peerAddress -d=ownDir
        for(String s : args){
            if(s.startsWith("-p="))
                ownPort = Integer.parseInt(s.substring(3));
            if(s.startsWith("-a=")){
                int delimiter = s.lastIndexOf(':');
                peerAddress = s.substring(3, delimiter);
                peerPort = Integer.parseInt(s.substring(delimiter+1));
            }
            if(s.startsWith("-d="))
                ownDir = s.substring(3);
            if(s.startsWith("-m=")){
                switch(s.charAt(3)){
                    case 'C':
                    case 'c':
                        mode = SINGLE_CLIENT;
                        break;
                    case 'S':
                    case 's':
                        mode = SINGLE_SERVER;
                        break;
                    case 'K':
                    case 'k':
                        mode = CLIENT_ONLY;
                        break;
                    case 'O':
                    case 'o':
                        mode = SERVER_ONLY;
                        break;
                }
            }
        }
        OwnState ownState = new OwnState(ownDir);
        if(mode == SINGLE_CLIENT || mode == SINGLE_SERVER) {
            SingleHostListener hostListener = new SingleHostListener(ownState, ownPort);
            Thread listenerThread = new Thread(hostListener);
            listenerThread.start();
            if(mode == SINGLE_SERVER){
                while(!hostListener.isConnected()) Thread.sleep(500);
            }
            new FileClient(peerAddress, peerPort, ownState).run();
            //  after closing client connection:
            Scanner scanner = new Scanner(System.in);
            while(listenerThread.isAlive()){
                System.out.println("Server thread is still connected. Terminate? (y/n)");
                if(scanner.next().toLowerCase().startsWith("y"))
                    hostListener.closeServer();
                Thread.sleep(500);
            }
        }
        if(mode == CLIENT_ONLY){
            new FileClient(peerAddress, peerPort, ownState).run();
        }
        if(mode == SERVER_ONLY){
            Listener listener = new Listener(ownState, ownPort);
            Thread listenerThread = new Thread(listener);
            listenerThread.start();
            Scanner scanner = new Scanner(System.in);
            do{
                System.out.println("Type 's' to stop listening for new connections");
            }while(scanner.next().startsWith("s"));
            listener.closeServer();
            System.out.println("Waiting for all clients to disconnect...");
        }
    }
}
