package Packet;

import Header.HeaderLiterals;

public class Bye extends Packet{
    public Bye(){
        super((char) 3);
        packet[2] = HeaderLiterals.bye;
    }
}
