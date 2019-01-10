package HostBehavior;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class FileSeeder extends Host implements Runnable{
    Socket socket;
    public FileSeeder(OwnState state, Socket socket){
        super(state);
        this.socket = socket;
    }
    @Override
    public void run() {
        try(
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream())
        ) {
            connectionLoop(in, out);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
