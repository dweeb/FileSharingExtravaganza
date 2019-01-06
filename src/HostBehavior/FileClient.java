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

public class FileClient implements Runnable{
    String server;
    int port;
    public FileClient(String server){
        this.server = server;
        this.port = port;
    }
    public static final int MAX_PACKET_SIZE = 1200;
    @Override
    public void run() {
        try(
            Socket socket = new Socket(server, port);
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            //
        ){
            if(!handshake(out, in)){throw new IncorrectBehaviorException();};
            //
            //  all necessary vars initialized here
            //
            FilesList serversList = new FilesList();
            byte lastFlag = HeaderLiterals.handshake;
            byte[] dataIn = new byte[MAX_PACKET_SIZE];
            byte[] temp = new byte[MAX_PACKET_SIZE];
            boolean inMiddleOfPacket = false;
            int bytesRead;
            int leftover = 0;
            char currentPacketLength = 0;
            int currentPacketDone = 0;
            //
            //  main loop
            //
            while((bytesRead = in.read(dataIn, leftover, MAX_PACKET_SIZE)) != -1){
                int bytesProcessed = 0;
                while(bytesRead - bytesProcessed >= 3){
                    if(!inMiddleOfPacket){
                        currentPacketLength = Packet.getNumberOfBytes(dataIn[bytesProcessed], dataIn[bytesProcessed+1]);
                        currentPacketDone = 0;
                        lastFlag = dataIn[bytesProcessed + 2];
                    }
                    int leftInDataIn = bytesRead - bytesProcessed;
                    int leftToCompleteTemp = currentPacketLength - currentPacketDone;
                    if(leftInDataIn < leftToCompleteTemp){
                        inMiddleOfPacket = true;
                        System.arraycopy(dataIn, bytesProcessed, temp, currentPacketDone, leftInDataIn);
                        currentPacketDone += leftInDataIn;
                        bytesProcessed = leftInDataIn;
                    } else {
                        inMiddleOfPacket = false;
                        System.arraycopy(dataIn, bytesProcessed, temp, currentPacketDone, leftToCompleteTemp);
                        bytesProcessed += leftToCompleteTemp;
                        currentPacketDone += leftToCompleteTemp;
                    }
                }
                if(bytesRead - bytesProcessed > 0){
                    leftover = bytesRead - bytesProcessed;
                    System.arraycopy(dataIn, bytesRead, dataIn, 0, leftover);
                } else leftover = 0;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IncorrectBehaviorException e) {
            System.err.println("Host terminated due to unknown behavior.");
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
    private void servicePacket(byte lastFlag){
        switch (lastFlag){
            case HeaderLiterals.fileListing : {
                break;
            }
            case HeaderLiterals.endOfListing : {
                break;
            }
            case HeaderLiterals.payload : {
                break;
            }
            default : {
                System.err.println("This was unexpected");
                break;
            }
        }
    }
}



/*      old stuff

                    if(!inMiddleOfPacket){
                        currentPacketLength = Packet.getNumberOfBytes(dataIn[bytesProcessed], dataIn[bytesProcessed+1]);
                        lastFlag = dataIn[bytesProcessed + 2];
                        if(bytesRead - bytesProcessed < currentPacketLength){
                            currentPacketDone = bytesRead - bytesProcessed;
                            inMiddleOfPacket = true;
                            System.arraycopy(dataIn, bytesProcessed, temp, 0, currentPacketDone);
                            bytesProcessed = bytesRead;
                        } else {
                            System.arraycopy(dataIn, bytesProcessed, temp, currentPacketDone, currentPacketLength - currentPacketDone);
                            bytesProcessed += currentPacketLength - currentPacketDone;
                        }
                    } else {
                        if(bytesRead - bytesProcessed < currentPacketLength - currentPacketDone){
                            System.arraycopy(dataIn, bytesProcessed, temp, currentPacketDone, bytesRead - bytesProcessed);
                            currentPacketDone += bytesRead - bytesProcessed;
                            bytesProcessed = bytesRead;
                        }
                    }
 */
