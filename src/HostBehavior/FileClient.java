package HostBehavior;

import Header.HeaderLiterals;
import Instance.OwnState;
import Packet.Handshake;
import Packet.ListingRequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class FileClient extends Host implements Runnable{
    String server;
    int port;
    public FileClient(String server, int port, OwnState ownState){
        super(ownState);
        this.server = server;
        this.port = port;
    }
    @Override
    public void run() {
        System.out.println(server + port);
        try(
            Socket socket = new Socket(server, port);
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            //
        ){
            if(!handshake(out, in)){throw new IncorrectBehaviorException();};
            out.write(new ListingRequest().getPacket());
            out.flush();
            connectionLoop(in, out);
        }
        catch (IOException e) {
            System.err.println("Peer has closed the connection.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IncorrectBehaviorException e) {
            System.err.println("Host terminated due to unknown behavior.");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            //  should not happen; MD5 is guaranteed in all implementations
            e.printStackTrace();
        }
    }
    //
}
