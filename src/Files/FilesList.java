package Files;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class FilesList {
    ArrayList<FilesListEntry> listing = new ArrayList<>();
    public FilesList(File path) throws NoSuchAlgorithmException {
        File[] files = path.listFiles();
        for(File f : files){
            if(!f.isDirectory())
                try {
                    listing.add(new FilesListEntry(f));
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
