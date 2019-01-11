package Packet;

import Header.HeaderLiterals;

public class Unavailable extends Packet{
    public Unavailable(){
        super((char)3);
        this.packet[2] = HeaderLiterals.unavailable;
    }
}
