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
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tempMeta));
        byte[] fileread = new byte[255];
        int bytesRead = bis.read(fileread, 0, 255);
        hash = Arrays.copyOfRange(fileread, 0, 16);
        size = Packet.byteArrToLong(Arrays.copyOfRange(fileread, 16, 24));
        filename = new String(Arrays.copyOfRange(fileread, 24, bytesRead));
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
    public long getCurrentSize(){
        return currentSize;
    }
}
