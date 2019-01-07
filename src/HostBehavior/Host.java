package HostBehavior;

import Header.HeaderLiterals;

public class Host {
    protected void servicePacket(byte[] packet){
        byte flag = packet[2];
        switch (flag){
            case HeaderLiterals.fileListing : {
                break;
            }
            case HeaderLiterals.endOfListing : {
                break;
            }
            case HeaderLiterals.payload : {
                break;
            }
            default : {
                System.err.println("This was unexpected");
                break;
            }
        }
    }
}
