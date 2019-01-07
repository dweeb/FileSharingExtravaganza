package Packet;

import Header.HeaderLiterals;

/**
 *      Fields in listing request packet:
 *          byte 0,1 : packet length. see Packet
 *          byte 2   : listing request flag flag
 */

public class ListingRequest extends Packet{
    public ListingRequest(byte[] packet) {
        super(packet);
    }
    public ListingRequest(){
        super((char)3);
        this.packet[2] = HeaderLiterals.listingRequest;
    }
}
