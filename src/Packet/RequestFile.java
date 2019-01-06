package Packet;

import Files.FilesListEntry;
import Header.HeaderLiterals;

/**
 *      Fields in request file packet:
 *          byte 0,1 : packet length. see Packet
 *          byte 2   : file request flag flag
 */

public class RequestFile extends Packet{
    RequestFile(FilesListEntry f){
        super((char)(3 + f.getFilename().getBytes().length));
        byte[] name = f.getFilename().getBytes();
        packet[2] = HeaderLiterals.requestFile;
        System.arraycopy(name, 0, packet, 3, name.length);
    }
}
