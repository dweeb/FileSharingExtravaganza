package Files;

import java.io.*;

public class TempFilesEntry {
    private File file;
    private byte[] hash;
    private long size;
    private long currentSize;
    private String name;
    protected TempFilesEntry(File temp, File tempMeta) throws IOException {
        file = temp;
        currentSize = file.length();
        BufferedReader br = new BufferedReader(new FileReader(tempMeta));
        hash = br.readLine().getBytes();
        size = Long.parseLong(br.readLine());
        name = br.readLine();
        br.close();
    }
    public String toString(){
        return name + " " + currentSize + " / " + size;
    }
}
