package Files;

import Packet.FileListing;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class FilesListEntry {
    byte[] hash;
    long size;
    String filename;
    public FilesListEntry(File f) throws NoSuchAlgorithmException, IOException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
        int bytesRead;
        size = 0;
        byte[] mb = new byte[1024];
        while((bytesRead = bis.read(mb)) != -1){
            md5.update(mb, 0, bytesRead);
            size += bytesRead;
        }
        hash = md5.digest();
        filename = f.getName();
    }
    public FilesListEntry(FileListing packet){
        size = packet.byteArrToLong(Arrays.copyOfRange(packet.getPacket(), 3, 11));
        hash = new byte[16];
        System.arraycopy(packet.getPacket(), 12, hash, 0, 16);
        filename = new String(Arrays.copyOfRange(packet.getPacket(), 30, packet.getPacket().length-1));
    }
    public byte[] getHash() {
        return hash;
    }
    public long getSize() {
        return size;
    }
    public String getFilename() {
        return filename;
    }
}
