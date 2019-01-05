package Packet;

import Header.HeaderLiterals;

/**
 *      Fields in handshake packet:
 *          bits 0,1 : packet length. see Packet
 *          bit 2 :    handshake flag
 */

public class Handshake extends Packet{
    public Handshake(){
        super((char)3);
        this.packet[2] = HeaderLiterals.handshake;
    }
    byte[] getPacket(){
        return packet;
    }
}
