package Instance;

import Files.FilesList;
import Files.FilesListEntry;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class OwnState {
    private FilesList fList;
    private String dir;
    public OwnState(String dir) throws NoSuchAlgorithmException {
        this.dir = dir;
        updateFList(dir);
    }
    public void updateFList(String dir) throws NoSuchAlgorithmException {
        fList = new FilesList(new File(dir));
    }
    public void addDownloadedFile(File f) throws IOException, NoSuchAlgorithmException {
        fList.add(new FilesListEntry(f));
    }
    public FilesList getfList(){
        return fList;
    }
    public String getDir(){
        return dir;
    }
}
