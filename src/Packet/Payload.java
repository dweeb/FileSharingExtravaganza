package Packet;

import Header.HeaderLiterals;

import java.util.Arrays;

/**
 *      Fields in payload packet:
 *          byte 0,1  : packet length. see Packet
 *          byte 2    : payload flag
 *          byte 3... : payload
 */

public class Payload extends Packet{
    public Payload(byte[] payload){
        super((char)(payload.length));
        packet[2] = HeaderLiterals.payload;
        //System.arraycopy(payload, 0, packet, 3, payload.length);
        //  line above is commented out as the payload is prepared beforehand in indices 3..end
    }
}
