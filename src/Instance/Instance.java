package Instance;

import java.security.NoSuchAlgorithmException;

import static Instance.Mode.*;

public class Instance {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        int ownPort;
        String peerAddress;
        int peerPort;
        String ownDir;
        //  program invocation : java -jar JARNAME -p=port -a=peerAddress -d=ownDir
        for(String s : args){
            if(s.startsWith("-p="))
                ownPort = Integer.parseInt(s.substring(3));
            if(s.startsWith("-a=")){
                int delimiter = s.lastIndexOf(':');
                peerAddress = s.substring(3, delimiter);
                peerPort = Integer.parseInt(s.substring(delimiter+1));
            }
            if(s.startsWith("-d="))
                ownDir = s.substring(3);
        }
    }
}
