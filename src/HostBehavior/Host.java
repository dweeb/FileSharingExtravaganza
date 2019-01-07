package HostBehavior;

import Files.FilesList;
import Files.FilesListEntry;
import Header.HeaderLiterals;
import Packet.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Host {
    public static final int MAX_PACKET_SIZE = 1030; //  actual max possible size should be 1027 given the specifics of payload packet
    public static final int DATA_IN_SIZE = 1024 * 8;
    private FilesList peersFilesList;
    private OwnState ownState;
    protected Host(OwnState ownState){
        peersFilesList = new FilesList();
        this.ownState = ownState;
    }
    protected void connectionLoop(BufferedInputStream in, BufferedOutputStream out) throws IOException {
        //
        //  all necessary vars initialized here
        //
        FilesList serversList = new FilesList();
        byte[] dataIn = new byte[DATA_IN_SIZE];
        byte[] temp = new byte[MAX_PACKET_SIZE];
        boolean inMiddleOfPacket = false;
        int bytesRead;
        int leftover = 0;
        char currentPacketLength = 0;
        int currentPacketDone = 0;
        //
        //  main loop
        //
        while((bytesRead = in.read(dataIn, leftover, DATA_IN_SIZE - leftover)) != -1){
            int bytesProcessed = 0;
            while(bytesRead - bytesProcessed >= 3){
                if(!inMiddleOfPacket){
                    currentPacketLength = Packet.getNumberOfBytes(dataIn[bytesProcessed], dataIn[bytesProcessed+1]);
                    currentPacketDone = 0;
                }
                int leftInDataIn = bytesRead - bytesProcessed;
                int leftToCompleteTemp = currentPacketLength - currentPacketDone;
                if(leftInDataIn < leftToCompleteTemp){
                    inMiddleOfPacket = true;
                    System.arraycopy(dataIn, bytesProcessed, temp, currentPacketDone, leftInDataIn);
                    currentPacketDone += leftInDataIn;
                    bytesProcessed = leftInDataIn;
                } else {
                    byte[] completedPacket;
                    if(!inMiddleOfPacket){
                        completedPacket = Arrays.copyOfRange(dataIn, bytesRead, bytesRead + currentPacketLength - 1);
                    } else {
                        inMiddleOfPacket = false;
                        System.arraycopy(dataIn, bytesProcessed, temp, currentPacketDone, leftToCompleteTemp);
                        completedPacket = Arrays.copyOfRange(temp, 0, currentPacketLength - 1);
                    }
                    bytesProcessed += leftToCompleteTemp;
                    currentPacketDone += leftToCompleteTemp;
                    //servicePacket(completedPacket);
                    //  op on completed packet
                    byte flag = completedPacket[2];
                    switch (flag){
                        case HeaderLiterals.handshake : {
                            out.write(new Handshake().getPacket(), 0, 3);
                            out.flush();
                            break;
                        }
                        case HeaderLiterals.listingRequest : {
                            Map<String, FilesListEntry> filesMap = ownState.getfList().getListing();
                            for(String k : filesMap.keySet()){
                                out.write(new FileListing(filesMap.get(k)).getPacket());
                            }
                            out.write(new EndOfListing().getPacket());
                            out.flush();
                            break;
                        }
                        case HeaderLiterals.fileListing : {
                            peersFilesList.add(new FilesListEntry(new FileListing(completedPacket)));
                            break;
                        }
                        case HeaderLiterals.endOfListing : {
                            //  show up DL menu
                            break;
                        }
                        case HeaderLiterals.payload : {
                            //  show up DL menu after DL is completed
                            break;
                        }
                        case HeaderLiterals.bye : {
                            out.close();
                            in.close();
                            break;
                        }
                        default : {
                            System.err.println("This was unexpected");
                            break;
                        }
                    }
                }
            }
            if(bytesRead - bytesProcessed > 0){
                leftover = bytesRead - bytesProcessed;
                System.arraycopy(dataIn, bytesRead, dataIn, 0, leftover);
            } else leftover = 0;
        }
    }
    void openDownloadMenu(BufferedOutputStream out) throws IOException {
        Map<String, FilesListEntry> filesMap = peersFilesList.getListing();
        ArrayList<FilesListEntry> filesListAsList = new ArrayList();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(String k : filesMap.keySet()){
            filesListAsList.add(filesMap.get(k));
            sb.append("" + (i++) + "\t:  " + filesMap.get(k).getFilename() + '\n');
        }
        String filesListAsString = new String(sb);
        displayDownloadMenu(filesListAsString);
        Scanner s = new Scanner(System.in);
        int choice;
        do{
            choice = s.nextInt();
        } while (choice >= filesListAsList.size());
        if(choice < 0){
            out.write(new Bye().getPacket());
        } else{
            out.write(new RequestFile(filesListAsList.get(choice)).getPacket());
        }
        out.flush();
    }
    void displayDownloadMenu(String s){
        System.out.println(
                "***********************\n" +
                "** PEER'S FILES LIST **\n" +
                "***********************\n"
        );
        System.out.println(s);
        System.out.print("Choose file to download,\n(or a negative value to end connection): ");
    }
}
