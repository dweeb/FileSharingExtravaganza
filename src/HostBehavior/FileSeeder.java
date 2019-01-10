package HostBehavior;

import java.net.Socket;

public class FileSeeder extends Host implements Runnable{
    Socket socket;
    public FileSeeder(OwnState state, Socket socket){
        super(state);
        this.socket = socket;
    }
    @Override
    public void run() {
        //
    }
}
