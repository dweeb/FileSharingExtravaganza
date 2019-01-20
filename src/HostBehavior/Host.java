package HostBehavior;

import Files.FilesList;
import Files.FilesListEntry;
import Files.TempFilesEntry;
import Header.HeaderLiterals;
import Instance.OwnState;
import Packet.*;

import java.io.*;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Host {
    public static final int MAX_PACKET_SIZE = 1024 + 3;//1030; //  actual max possible size should be 1027 given the specifics of payload packet
    public static final int DATA_IN_SIZE = 1024 * 8;
    protected OwnState ownState;
    private FilesList peersFilesList;
    protected Host(OwnState ownState){
        this.ownState = ownState;
        peersFilesList = new FilesList();
        //  all necessary vars initialized here - init is needed because the main loop and file processing is realised using
        //  a very simple finite-state automaton which checks the type of packet it got sequentially and then processes it
        dataIn = new byte[DATA_IN_SIZE];
        temp = new byte[MAX_PACKET_SIZE];
        inMiddleOfPacket = false;
        leftover = 0;
        currentPacketLength = 0;
        currentPacketDone = 0;
        fileOut = null;
        digestOut = null;
        currentFile = null;
        fileDownloadCompleted = 0;
        packet = new Packet(null);
        tempFile = null;
        progressCounterIterator = 0;
    }
    private byte[] dataIn;
    private byte[] temp;
    private boolean inMiddleOfPacket;
    private int bytesRead;
    private int leftover;
    private char currentPacketLength;
    private int currentPacketDone;
    private BufferedOutputStream fileOut;
    private DigestOutputStream digestOut;
    private FilesListEntry currentFile;
    private long fileDownloadCompleted;
    private Packet packet;
    private File tempFile;
    private File tempMeta;
    private int progressCounterIterator;
    //
    protected void connectionLoop(BufferedInputStream in, BufferedOutputStream out) throws IOException, NoSuchAlgorithmException, InterruptedException {
        try {
            //
            //  main loop
            //
            while ((bytesRead = in.read(dataIn, leftover, DATA_IN_SIZE - leftover) + leftover) != -1) {
                //  leftover added to bytesRead because the leftover bytes are added at the start of the array, effectively extending it
                int bytesProcessed = 0;
                while (bytesRead - bytesProcessed >= 3) {
                    if (!inMiddleOfPacket) {
                        currentPacketLength = packet.getNumberOfBytes(dataIn[bytesProcessed], dataIn[bytesProcessed + 1]);
                        currentPacketDone = 0;
                    }
                    int leftInDataIn = bytesRead - bytesProcessed;
                    int leftToCompleteTemp = currentPacketLength - currentPacketDone;
                    if (leftInDataIn < leftToCompleteTemp) {
                        inMiddleOfPacket = true;
                        System.arraycopy(dataIn, bytesProcessed, temp, currentPacketDone, leftInDataIn);
                        currentPacketDone += leftInDataIn;
                        bytesProcessed += leftInDataIn;  //  number of hours spent finding that there should be "+=" instead of "=" : 2
                    } else {
                        byte[] completedPacket;
                        if (!inMiddleOfPacket) {
                            completedPacket = Arrays.copyOfRange(dataIn, bytesProcessed, bytesProcessed + currentPacketLength);
                        } else {
                            inMiddleOfPacket = false;
                            System.arraycopy(dataIn, bytesProcessed, temp, currentPacketDone, leftToCompleteTemp);
                            completedPacket = Arrays.copyOfRange(temp, 0, currentPacketLength);
                        }
                        bytesProcessed += leftToCompleteTemp;
                        currentPacketDone += leftToCompleteTemp;
                        //  op on completed packet
                        servicePacket(in, out, completedPacket);
                    }
                }
                if (bytesRead - bytesProcessed > 0) {
                    leftover = bytesRead - bytesProcessed;
                    System.arraycopy(dataIn, bytesProcessed, dataIn, 0, leftover);
                } else leftover = 0;
                if (bytesRead < 3)
                    Thread.sleep(200);  //  if there's nothing useful added by peer, wait a bit to avoid a busy tight loop
            }
        }catch (IOException e){
            if (fileOut != null)
                fileOut.close();
            throw new IOException();
        }
    }
    private FilesListEntry openDownloadMenu(BufferedOutputStream out) throws IOException, NoSuchAlgorithmException {
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
                FilesListEntry checkedEntry = (FilesListEntry) ownState.getfList().getListing().get(new String(result.getMD5Hash()));
                if (checkedEntry != null) {
                    if(new File(ownState.getDir() + File.separator + checkedEntry.getFilename()).exists()) {
                        fileAlreadyExists = true;
                        System.out.println("Requested file already exists in the downloads directory.");
                    }else{
                        ownState.getfList().getListing().remove(new String(result.getMD5Hash()));
                        fileAlreadyExists = false;
                    }
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
    private void displayDownloadMenu(String s){
        System.out.println(
                "***********************\n" +
                "** PEER'S FILES LIST **\n" +
                "***********************\n"
        );
        System.out.println(s);
        System.out.print("Choose file to download,\n(or a negative value to end connection): ");
    }
    private void seed(byte[] packet, BufferedOutputStream out) throws IOException {
        String key = new String(Arrays.copyOfRange(packet, 3, 19));
        long startAt = Packet.byteArrToLong(Arrays.copyOfRange(packet, 19, packet.length));
        FilesListEntry file = (FilesListEntry) ownState.getfList().getListing().get(key);
        //System.out.println(ownState.getDir());
        boolean fileAvailable = true;
        if(file != null) {
            File fileToSeed = new File("" + ownState.getDir() + File.separator + file.getFilename());
            if (fileToSeed.exists()) {
                try (
                        BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(fileToSeed)); //lisp lookalike
                ) {
                    if (startAt > 0) fileIn.skip(startAt);
                    byte sendOut[] = new byte[MAX_PACKET_SIZE];
                    int fileRead;
                    while ((fileRead = fileIn.read(sendOut, 0, 1024)) != -1) {
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
            } else fileAvailable = false;
        }else fileAvailable = false;
        if(!fileAvailable){
            ownState.getfList().getListing().remove(key);
            out.write(new Unavailable().getPacket());
        }
        out.flush();
    }
    boolean handshake(BufferedOutputStream out, BufferedInputStream in) throws IOException, InterruptedException {
        out.write(new Handshake().getPacket());
        out.flush();
        byte[] b = new byte[3];
        int read = 0;
        while((read += in.read(b, read, 3-read)) != 3){Thread.sleep(10);}
        if(new Handshake(b).getNumberOfBytes() == 3 && b[2] == HeaderLiterals.handshake) return true;
        return false;
    }
    private void servicePacket(BufferedInputStream in, BufferedOutputStream out, byte[] completedPacket) throws IOException, NoSuchAlgorithmException {
        byte flag = completedPacket[2];
        try {
            switch (flag) {
                case HeaderLiterals.handshake:
                    returnHandshake(out);
                    break;
                case HeaderLiterals.listingRequest:
                    postListing(out);
                    break;
                case HeaderLiterals.fileListing:
                    peersFilesList.add(new FilesListEntry(new FileListing(completedPacket)));
                    break;
                case HeaderLiterals.unavailable:
                    System.out.println("Server says: File unavailable.");   //  yes, it is supposed to roll into another file choice
                case HeaderLiterals.endOfListing:
                    chooseNextFileToDownload(out);
                    break;
                case HeaderLiterals.requestFile:
                    seed(completedPacket, out);
                    break;
                case HeaderLiterals.payload:
                    servicePayload(out, completedPacket);
                    break;
                case HeaderLiterals.bye:
                    out.close();
                    in.close();
                    break;
                default:
                    System.err.println("This was unexpected");
                    break;
            }
        }catch (FileOperationException e){
            System.err.println("File operation could not be completed.");
        }
    }
    private void returnHandshake(BufferedOutputStream out) throws IOException {
        out.write(new Handshake().getPacket(), 0, 3);
        out.flush();
    }
    private void postListing(BufferedOutputStream out) throws IOException {
        Map<String, FilesListEntry> filesMap = ownState.getfList().getListing();
        for(String k : filesMap.keySet()){
            out.write(new FileListing(filesMap.get(k)).getPacket());
        }
        out.write(new EndOfListing().getPacket());
        out.flush();
    }
    private void chooseNextFileToDownload(BufferedOutputStream out) throws IOException, NoSuchAlgorithmException, FileOperationException {
        int continued = checkForTempFile();
        if(continued < 0) {
            currentFile = openDownloadMenu(out);
            if (currentFile != null)
                try {
                    Random r = new Random();
                    int tempNumber = r.nextInt();
                    tempFile = new File(ownState.getDir() + File.separator + ".temp_" + tempNumber);
                    tempMeta = new File(ownState.getDir() + File.separator + ".temp_" + tempNumber + "meta");
                    createTempMeta(tempMeta);
                    digestOut = new DigestOutputStream(
                            new FileOutputStream(tempFile),
                            MessageDigest.getInstance("MD5")
                    );
                    fileOut = new BufferedOutputStream(digestOut);
                } catch (IOException e) {
                    throw new FileOperationException();
                }
            fileDownloadCompleted = 0;
        } else {
            TempFilesEntry currentContinued = ownState.getfList().getTempFilesList().get(continued);
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                BufferedInputStream bis = new BufferedInputStream(new DigestInputStream(new FileInputStream(currentContinued.getFile()), md));
                byte b[] = new byte[1024];
                while(bis.read(b) != -1){}
                bis.close();
                tempFile = currentContinued.getFile();
                tempMeta = currentContinued.getMetaFile();
                digestOut = new DigestOutputStream(
                        new FileOutputStream(tempFile, true),
                        md
                );
                fileOut = new BufferedOutputStream(digestOut);
                currentFile = currentContinued;
                ownState.getfList().getTempFilesList().remove(currentContinued);
            } catch (IOException e){
                throw new FileOperationException();
            }
            out.write(new RequestFile(currentContinued).getPacket());
            out.flush();
            fileDownloadCompleted = currentContinued.getCurrentSize();
            System.out.println("Debug:file chosen");
        }

    }
    private void servicePayload(BufferedOutputStream out, byte[] completedPacket) throws NoSuchAlgorithmException, FileOperationException, IOException {
        try {
            fileOut.write(completedPacket, 3, completedPacket.length - 3);
        } catch (IOException e){
            throw new FileOperationException();
        }
        fileDownloadCompleted = fileDownloadCompleted + (completedPacket.length - 3);
        if(progressCounterIterator >= 50 || fileDownloadCompleted==currentFile.getSize()) {
            System.out.println(fileDownloadCompleted + " / " + currentFile.getSize());
            progressCounterIterator = 0;
        } else ++progressCounterIterator;
        if(fileDownloadCompleted >= currentFile.getSize()){
            try {
                fileOut.flush();
                byte[] b = digestOut.getMessageDigest().digest();
                fileOut.close();
                if (Arrays.equals(b, currentFile.getMD5Hash())) {
                    System.out.println("File download completed. MD5 checksum in order");
                } else {
                    System.out.println("File downloaded, MD5 hash different from expected!");
                }
                renameFile(tempFile);
                tempMeta.delete();
            }catch (IOException e){
                throw new FileOperationException();
            }
            ownState.getfList().add(currentFile);
            //
            chooseNextFileToDownload(out);
        }

    }
    private void renameFile(File file){
        StringBuilder newName = new StringBuilder(currentFile.getFilename());
        if(new File(currentFile.getFilename()).exists()) {
            int dot = newName.lastIndexOf(".");
            Random r = new Random();
            newName.insert(dot, "_" + r.nextInt());
        }
        newName.insert(0, ownState.getDir() + File.separator);
        file.renameTo(new File(new String(newName)));
    }
    private void createTempMeta(File tempMeta) throws IOException {
        BufferedOutputStream tempOut = new BufferedOutputStream(new FileOutputStream(tempMeta));
        tempOut.write(currentFile.getMD5Hash());
        tempOut.write(Packet.longToByteArr(currentFile.getSize()));
        tempOut.write(currentFile.getFilename().getBytes());
        tempOut.flush();
        tempOut.close();
    }
    private int checkForTempFile() {
        ArrayList<TempFilesEntry> tempsList = ownState.getfList().getTempFilesList();
        if(!tempsList.isEmpty()){
            return chooseToContinueDownload(tempsList);
        }
        else return -1;
    }
    private int chooseToContinueDownload(ArrayList<TempFilesEntry> tempsList) {
        System.out.println("There are some unfinished downloads. Do you want to resume them?");
        for(int i=0; i<tempsList.size(); i++){
            System.out.println("" + i + "\t: " + tempsList.get(i).toString());
        }
        System.out.print("Your choice (integer out of range will download a new file instead):");
        Scanner s = new Scanner(System.in);
        int choice = s.nextInt();
        if(choice >= tempsList.size()) choice = -1;
        return choice;
    }
}
