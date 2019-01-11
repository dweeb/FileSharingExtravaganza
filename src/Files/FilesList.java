package Files;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

public class FilesList {    // actually a map
    private ConcurrentHashMap<String, FilesListEntry> listing;
    public FilesList(File path) throws NoSuchAlgorithmException {
        listing = new ConcurrentHashMap<String, FilesListEntry>();
        File[] files = path.listFiles();
        for(File f : files){
            if(!f.isDirectory())
                try {
                    FilesListEntry e = new FilesListEntry(f);
                    add(e);
                    listing.put(new String(e.getMD5Hash()), e);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    public FilesList(){
        listing = new ConcurrentHashMap<String, FilesListEntry>();
    }
    public void add(FilesListEntry e){
        listing.put(new String(e.getMD5Hash()), e);
    }
    public ConcurrentHashMap getListing(){
        return listing;
    }
}
