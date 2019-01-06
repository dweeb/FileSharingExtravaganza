package Files;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class FilesList {
    private ArrayList<FilesListEntry> listing;
    public FilesList(File path) throws NoSuchAlgorithmException {
        listing = new ArrayList<>();
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
    public FilesList(){
        listing = new ArrayList<>();
    }
    public void add(FilesListEntry e){
        listing.add(e);
    }
}
