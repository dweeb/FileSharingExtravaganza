package Packet;

import Header.HeaderLiterals;

public class ListingRequest extends Packet{
    public ListingRequest(byte[] packet) {
        super(packet);
    }
    public ListingRequest(){
        super((char)3);
        this.packet[2] = HeaderLiterals.listingRequest;
    }
}
