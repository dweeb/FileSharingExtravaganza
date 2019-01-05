package Packet;

/**
 *      Fields in packet:
 *          bits 0,1 : packet length. Generated from a char (the only unsigned type, with a very fitting range and size)
 *              at origin and consolidated back into a char at destination using getBytes()
 */

public class Packet {
    protected byte[] packet;
    protected Packet(char noBytes){
        this.packet = new byte[noBytes];
        this.packet[0] = (byte) noBytes;
        this.packet[1] = (byte)(noBytes >> 8);
    }
    public Packet(byte[] packet){
        this.packet = packet;
    }
    protected Packet(){};
    public char getBytes(){
        return (char)(
                        (packet[0] & 0xFF)              //  <- has to be masked to avoid signed-to-unsigned shenanigans
                                |                       //  (bit shift seems to implicitly cast everything to int, which
                                                        //  is signed and thus fills empty space with ones for negative
                                                        //  numbers)
                        (char)((char)packet[1] << 8)    //  t. spent half an hour getting this right
        );
    }
}
