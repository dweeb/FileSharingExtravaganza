package Packet;

import Files.FilesListEntry;
import Header.HeaderLiterals;

/**
 *      Fields in FileListing packet:
 *          byte 0,1    : packet length. see Packet
 *          byte 2      : listing flag
 *          byte 3..10  : filesize
 *          byte 11..26 : MD5 hash
 *          byte 27...  : filename
 */

public class FileListing extends Packet{
    public FileListing(byte[] packet) {
        super(packet);
    }
    public FileListing(FilesListEntry entry){
        byte[] size = longToByteArr(entry.getSize());
        byte[] hash = entry.getMD5Hash();
        byte[] name = entry.getFilename().getBytes();
        this.packet = new byte[3 + size.length + hash.length + name.length];
        this.packet[0] = (byte) packet.length;
        this.packet[1] = (byte)(packet.length >> 8);
        this.packet[2] = HeaderLiterals.fileListing;
        System.arraycopy(size, 0, packet, 3, size.length);
        System.arraycopy(hash, 0, packet, 3+size.length, hash.length);
        System.arraycopy(name, 0, packet, 3+size.length+hash.length, name.length);
    }
}
