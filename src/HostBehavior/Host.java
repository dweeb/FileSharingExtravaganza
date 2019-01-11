package HostBehavior;

import Files.FilesList;
import Files.FilesListEntry;
import Header.HeaderLiterals;
import Instance.OwnState;
import Packet.*;

import java.io.*;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Host {
    public static final int MAX_PACKET_SIZE = 1024 + 3;//1030; //  actual max possible size should be 1027 given the specifics of payload packet
    public static final int DATA_IN_SIZE = 1024 * 8;
    private FilesList peersFilesList;
    protected OwnState ownState;
    protected Host(OwnState ownState){
        peersFilesList = new FilesList();
        this.ownState = ownState;
    }
    protected void connectionLoop(BufferedInputStream in, BufferedOutputStream out) throws IOException, NoSuchAlgorithmException, InterruptedException {
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
        BufferedOutputStream fileOut = null;
        DigestOutputStream digestOut = null;
        FilesListEntry currentFile = null;
        long fileDownloadCompleted = 0;
        Packet p = new Packet(null);
        //
        //  main loop
        //
        while((bytesRead = in.read(dataIn, leftover, DATA_IN_SIZE - leftover) + leftover) != -1){
            //  leftover added to bytesRead because it is added at the start of the array, effectively extending it
            int bytesProcessed = 0;
            while(bytesRead - bytesProcessed >= 3){
                if(!inMiddleOfPacket){
                    currentPacketLength = p.getNumberOfBytes(dataIn[bytesProcessed], dataIn[bytesProcessed+1]);
                    currentPacketDone = 0;
                }
                int leftInDataIn = bytesRead - bytesProcessed;
                int leftToCompleteTemp = currentPacketLength - currentPacketDone;
                if(leftInDataIn < leftToCompleteTemp){
                    inMiddleOfPacket = true;
                    System.arraycopy(dataIn, bytesProcessed, temp, currentPacketDone, leftInDataIn);
                    currentPacketDone += leftInDataIn;
                    bytesProcessed += leftInDataIn;  //  number of hours spent finding that there should be "+=" instead of "=" : 2
                } else {
                    byte[] completedPacket;
                    if(!inMiddleOfPacket){
                        completedPacket = Arrays.copyOfRange(dataIn, bytesProcessed, bytesProcessed + currentPacketLength);
                    } else {
                        inMiddleOfPacket = false;
                        System.arraycopy(dataIn, bytesProcessed, temp, currentPacketDone, leftToCompleteTemp);
                        completedPacket = Arrays.copyOfRange(temp, 0, currentPacketLength);
                    }
                    bytesProcessed += leftToCompleteTemp;
                    currentPacketDone += leftToCompleteTemp;
                    //servicePacket(completedPacket);
                    //  op on completed packet
                    byte flag = completedPacket[2];
                    switch (flag){
                        case HeaderLiterals.handshake :
                            out.write(new Handshake().getPacket(), 0, 3);
                            out.flush();
                            break;
                        case HeaderLiterals.listingRequest :
                            Map<String, FilesListEntry> filesMap = ownState.getfList().getListing();
                            for(String k : filesMap.keySet()){
                                out.write(new FileListing(filesMap.get(k)).getPacket());
                            }
                            out.write(new EndOfListing().getPacket());
                            out.flush();
                            break;
                        case HeaderLiterals.fileListing :
                            peersFilesList.add(new FilesListEntry(new FileListing(completedPacket)));
                            break;
                        case HeaderLiterals.endOfListing :
                            currentFile = openDownloadMenu(out);
                            if(currentFile != null)
                                digestOut = new DigestOutputStream(
                                        new FileOutputStream(ownState.getDir() + File.separator + currentFile.getFilename()),
                                        MessageDigest.getInstance("MD5")
                                );
                            fileOut = new BufferedOutputStream(digestOut);
                            fileDownloadCompleted = 0;
                            break;
                        case HeaderLiterals.requestFile :
                            seed(new String(Arrays.copyOfRange(completedPacket, 3, 19)), out);
                            break;
                        case HeaderLiterals.payload :
                            fileOut.write(completedPacket, 3, completedPacket.length - 3);
                            fileDownloadCompleted = fileDownloadCompleted + (completedPacket.length - 3);
                            System.out.println(fileDownloadCompleted + " / " + currentFile.getSize());
                            if(fileDownloadCompleted >= currentFile.getSize()){
                                fileOut.flush();
                                byte[] b = digestOut.getMessageDigest().digest();
                                fileOut.close();
                                if(Arrays.equals(b, currentFile.getMD5Hash())){
                                    System.out.println("File download completed. MD5 checksum in order");
                                } else {
                                    System.out.println("File downloaded, MD5 hash different from expected!");
                                }
                                //
                                currentFile = openDownloadMenu(out);
                                if(currentFile != null)
                                    digestOut = new DigestOutputStream(
                                            new FileOutputStream(ownState.getDir() + File.separator + currentFile.getFilename()),
                                            MessageDigest.getInstance("MD5")
                                    );
                                fileOut = new BufferedOutputStream(digestOut);
                                fileDownloadCompleted = 0;
                            }
                            break;
                        case HeaderLiterals.bye :
                            out.close();
                            in.close();
                            break;
                        default :
                            System.err.println("This was unexpected");
                            break;
                    }
                }
            }
            if(bytesRead - bytesProcessed > 0){
                leftover = bytesRead - bytesProcessed;
                System.arraycopy(dataIn, bytesRead, dataIn, 0, leftover);
            } else leftover = 0;
            if (bytesRead < 3)
                Thread.sleep(200);  //  if there's nothing useful added by peer, wait a bit to avoid a busy tight loop
        }
    }
    FilesListEntry openDownloadMenu(BufferedOutputStream out) throws IOException, NoSuchAlgorithmException {
        Map<String, FilesListEntry> filesMap = peersFilesList.getListing();
        ArrayList<FilesListEntry> filesListAsList = new ArrayList();
        StringBuilder sb = new StringBuilder();
        boolean fileAlreadyExists;
        int i = 0;
        for(String k : filesMap.keySet()){
            filesListAsList.add(filesMap.get(k));
            sb.append("" + (i++) + "\t:  " + filesMap.get(k).getFilename() + '\n');
        }
        String filesListAsString = new String(sb);
        int choice;
        FilesListEntry result;
        Scanner s = new Scanner(System.in);
        do {
            displayDownloadMenu(filesListAsString);
            do {
                choice = s.nextInt();
            } while (choice >= filesListAsList.size());
            if (choice < 0) {
                result = null;
                fileAlreadyExists = false;
            } else {
                result = filesListAsList.get(choice);
                if (ownState.getfList().getListing().get(new String(result.getMD5Hash())) != null) {
                    fileAlreadyExists = true;
                    System.out.println("Requested file already exists in the downloads directory.");
                } else
                    fileAlreadyExists = false;
            }
        } while (fileAlreadyExists);
        if(choice < 0){
            out.write(new Bye().getPacket());
        } else{
            out.write(new RequestFile(filesListAsList.get(choice)).getPacket());
        }
        out.flush();
        return result;
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
    void seed(String key, BufferedOutputStream out) throws IOException {
        FilesListEntry file = (FilesListEntry) ownState.getfList().getListing().get(key);
        System.out.println(ownState.getDir());
        if(file != null){
            try (
                BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream( new File(
                        "" + ownState.getDir() + File.separator + file.getFilename()))); //lisp lookalike
            ) {
                byte sendOut[] = new byte[MAX_PACKET_SIZE];
                int fileRead;
                while((fileRead = fileIn.read(sendOut, 0, 1024)) != -1){
                    out.write(new Payload(Arrays.copyOfRange(sendOut, 0, fileRead)).getPacket());
                }   //  for the longest time I thought Arrays.copyOfRange() creates an array that points at the existing part
                    //  of the source array, and doesn't actually copy the *values* - too late to turn back now
            } catch (FileNotFoundException e) {
                out.write(new Unavailable().getPacket());
                System.err.println("Requested file was not found");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            out.write(new Unavailable().getPacket());
        }
        out.flush();
    }
    protected boolean handshake(BufferedOutputStream out, BufferedInputStream in) throws IOException, InterruptedException {
        out.write(new Handshake().getPacket());
        out.flush();
        byte[] b = new byte[3];
        int read = 0;
        while((read += in.read(b, read, 3-read)) != 3){Thread.sleep(10);}
        if(new Handshake(b).getNumberOfBytes() == 3 && b[2] == HeaderLiterals.handshake) return true;
        return false;
    }
}
