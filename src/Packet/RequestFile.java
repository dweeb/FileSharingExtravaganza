package Packet;

import Files.FilesListEntry;
import Header.HeaderLiterals;

/**
 *      Fields in request file packet:
 *          byte 0,1    : packet length. see Packet
 *          byte 2      : file request flag flag
 *          byte 3..18  : hash (was filename at first)
 *          byte 19..34 : start at position in file
 */

public class RequestFile extends Packet{
    public RequestFile(FilesListEntry f){
        super((char)(3 + f.getMD5Hash().length + 8));
        byte[] hash = f.getMD5Hash();
        packet[2] = HeaderLiterals.requestFile;
        System.arraycopy(hash, 0, packet, 3, hash.length);
    }
    public RequestFile(FilesListEntry f, long startAt){
        this(f);
        System.arraycopy(longToByteArr(startAt), 0, packet, 19, 8);
    }
}
