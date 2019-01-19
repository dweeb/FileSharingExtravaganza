package Files;

import Packet.Packet;

import java.io.*;
import java.util.Arrays;

public class TempFilesEntry extends FilesListEntry {
    private File file;
    private File metaFile;
    private long currentSize;
    protected TempFilesEntry(File temp, File tempMeta) throws IOException {
        file = temp;
        metaFile = tempMeta;
        currentSize = file.length();
        /*
        BufferedReader br = new BufferedReader(new FileReader(tempMeta));
        hash = br.readLine().getBytes();
        size = Long.parseLong(br.readLine());
        filename = br.readLine();
        br.close();
        */
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tempMeta));
        byte[] fileread = new byte[128];
        int bytesRead = bis.read(fileread, 0, 128);
        hash = Arrays.copyOfRange(fileread, 0, 16);
        size = Packet.byteArrToLong(Arrays.copyOfRange(fileread, 16, 24));
        filename = new String(Arrays.copyOfRange(fileread, 24, bytesRead+1));
        bis.close();
        System.out.println(hash.length + "  " + size + "  " + filename);
    }
    public String toString(){
        return filename + " " + currentSize + " / " + size;
    }
    public File getFile(){
        return file;
    }
    public File getMetaFile(){
        return metaFile;
    }
    public long getSize(){
        return size;
    }
    public long getCurrentSize(){
        return currentSize;
    }
    public String getName(){
        return filename;
    }
    public byte[] getMD5Hash(){
        return hash;
    }
}
