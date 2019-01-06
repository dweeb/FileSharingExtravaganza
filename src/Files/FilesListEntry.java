package Files;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
