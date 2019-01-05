import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class FileClient implements Runnable{
    String server;
    int port;
    public FileClient(String server){
        this.server = server;
        this.port = port;
    }
    @Override
    public void run() {
        try(
            Socket socket = new Socket(server, port);
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            //
        ){
            byte[] packet = new byte[1400];
            int bytesDownladed = 0;
            handshake(out);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    //
    private boolean handshake(BufferedOutputStream out){
        return false;
    }
}
