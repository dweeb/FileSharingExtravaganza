package HostBehavior;

import Files.FilesList;
import Files.FilesListEntry;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class OwnState {
    private FilesList fList;
    public OwnState(String dir) throws NoSuchAlgorithmException {
        updateFList(dir);
    }
    void updateFList(String dir) throws NoSuchAlgorithmException {
        fList = new FilesList(new File(dir));
    }
    void addDownloadedFile(File f) throws IOException, NoSuchAlgorithmException {
        fList.add(new FilesListEntry(f));
    }
    FilesList getfList(){
        return getfList();
    }
}
