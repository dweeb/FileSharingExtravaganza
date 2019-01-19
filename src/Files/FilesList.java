package Files;

import HostBehavior.FileOperationException;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class FilesList {    // actually a map
    private ConcurrentHashMap<String, FilesListEntry> listing;
    private ArrayList<TempFilesEntry> tempFilesList;
    public FilesList(File path) throws NoSuchAlgorithmException, FileOperationException, IOException {
        listing = new ConcurrentHashMap<String, FilesListEntry>();
        ArrayList<File> tempFiles = new ArrayList<File>();
        ArrayList<File> tempMetas = new ArrayList<File>();
        File[] files = path.listFiles();
        for(File f : files){
            if(!f.isDirectory())
                try {
                    if(!f.getName().startsWith(".temp_")) {
                        FilesListEntry e = new FilesListEntry(f);
                        add(e);
                        listing.put(new String(e.getMD5Hash()), e);
                    } else {
                        if(!f.getName().endsWith("meta"))
                            tempFiles.add(f);
                        else
                            tempMetas.add(f);
                    }
                } catch (IOException e) {
                    throw new FileOperationException();
                }
        }
        System.out.println(tempFiles.size() + "   " + tempMetas.size());
        tempFilesList = new ArrayList<>();
        for(File tf : tempFiles){
            String metaName = tf.getName() + "meta";
            for(File mf : tempMetas){
                if(metaName.equals(mf.getName())){
                    tempFilesList.add(new TempFilesEntry(tf, mf));
                }
            }
        }
        /*
        for(File f : tempFiles){
            int i=0;
            while(i<tempMetas.size() && !tempMetas.get(i).getName().startsWith(f.getName())){ i++; }
            tempFilesList.add(new TempFilesEntry(f, tempMetas.get(i)));
            tempMetas.remove(i);
        }
        */
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
    public ArrayList<TempFilesEntry> getTempFilesList(){
        return tempFilesList;
    }
}
