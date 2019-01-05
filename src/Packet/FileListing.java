package Packet;

import Files.FilesListEntry;
import Header.HeaderLiterals;

public class FileListing extends Packet{
    public FileListing(byte[] packet) {
        super(packet);
    }
    public FileListing(FilesListEntry entry){
        byte[] size = longToByteArr(entry.getSize());
        byte[] hash = entry.getHash();
        byte[] name = entry.getFilename().getBytes();
        this.packet = new byte[3 + size.length + hash.length + name.length];
        this.packet[2] = HeaderLiterals.fileListing;
        System.arraycopy(size, 0, packet, 3, size.length);
        System.arraycopy(hash, 0, packet, 3+size.length, hash.length);
        System.arraycopy(name, 0, packet, 3+size.length+hash.length, name.length);
    }
    public byte[] longToByteArr(long l){
        int arrSize = Long.SIZE / Byte.SIZE;
        byte[] result = new byte[arrSize];
        for(int i=0; i<arrSize; i++){
            result[i] = (byte)((l>>(i*8)) & 0xFF);
        }
        return result;
    }
    public long byteArrToLong(byte[] b){
        long result = 0;
        for(int i=0; i<b.length; i++){
            result |= (b[i] & 0xFFl) << (i*8);
        }
        return result;
    }
}
