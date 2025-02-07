package HostBehavior;

import Instance.OwnState;

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
            System.out.println("Connection with client was terminated.");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
