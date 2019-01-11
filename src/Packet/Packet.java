package Packet;

/**
 *      Fields in packet:
 *          byte 0,1 : packet length. Generated from a char (the only unsigned type, with a very fitting range and size)
 *              at origin and consolidated back into a char at destination using getNumberOfBytes()
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
    public char getNumberOfBytes(){
        return (char)(
                        (packet[0] & 0xFF)              //  has to be masked because casting a signed negative byte
                                |                       //  to a larger type fills the space with 1's
                        (char)((char)packet[1] << 8)    //  t. spent half an hour getting this right
        );
    }
    public char getNumberOfBytes(byte b0, byte b1){
        return (char)(
                (b0 & 0xFF) | (char)((char)b1 << 8)     //  copy of the above for before a packet is extracted from data
        );
    }
    public byte[] getPacket(){ return packet;}
}
