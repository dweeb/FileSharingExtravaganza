package Packet;

import Files.FilesListEntry;
import Header.HeaderLiterals;

/**
 *      Fields in request file packet:
 *          byte 0,1  : packet length. see Packet
 *          byte 2    : file request flag flag
 *          byte 3... : hash (was filename at first)
 */

public class RequestFile extends Packet{
    public RequestFile(FilesListEntry f){
        super((char)(3 + f.getHash().length));
        byte[] hash = f.getHash();
        packet[2] = HeaderLiterals.requestFile;
        System.arraycopy(hash, 0, packet, 3, hash.length);
    }
}
