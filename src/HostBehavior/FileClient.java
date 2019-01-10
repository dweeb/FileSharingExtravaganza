package HostBehavior;

import Files.FilesList;
import Header.HeaderLiterals;
import Packet.Handshake;
import Packet.ListingRequest;
import Packet.Packet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class FileClient extends Host implements Runnable{
    String server;
    int port;
    public FileClient(String server, int port, OwnState ownState){
        super(ownState);
        this.server = server;
        this.port = port;
        this.ownState = ownState;
    }
    @Override
    public void run() {
        try(
            Socket socket = new Socket(server, port);
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            //
        ){
            if(!handshake(out, in)){throw new IncorrectBehaviorException();};
            connectionLoop(in, out);
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IncorrectBehaviorException e) {
            System.err.println("Host terminated due to unknown behavior.");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    //
    private boolean handshake(BufferedOutputStream out, BufferedInputStream in) throws IOException, InterruptedException {
        out.write(new Handshake().getPacket(), 0, 3);
        out.flush();
        byte[] b = new byte[3];
        int read = 0;
        while((read += in.read(b, read, 3-read)) != 3){Thread.sleep(10);}
        if(new Handshake(b).getNumberOfBytes() == 3 && b[2] == HeaderLiterals.handshake) return true;
        return false;
    }
}
