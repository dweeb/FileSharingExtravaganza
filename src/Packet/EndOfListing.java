package Packet;

import Header.HeaderLiterals;

/**
 *      Fields in endOfListing packet:
 *          byte 0,1 : packet length. see Packet
 *          byte 2   : endOfListing flag
 */

public class EndOfListing extends Packet{
    public EndOfListing(){
        super((char)3);
        this.packet[2] = HeaderLiterals.endOfListing;
    }
}
