package HostBehavior;

import java.net.Socket;

public class FileSeeder implements Runnable{
    Socket socket;
    public FileSeeder(Socket socket){
        this.socket = socket;
    }
    @Override
    public void run() {
        //
    }
}
