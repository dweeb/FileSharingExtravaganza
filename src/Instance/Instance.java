package Instance;

import java.security.NoSuchAlgorithmException;

import static Instance.Mode.*;

public class Instance {
    static Mode mode;
    static String ownDir;
    public static void main(String[] args) throws NoSuchAlgorithmException {
        //  program invocation : java -jar JARNAME -m=mode -d=instance\'s\ directory
        mode = PEER;
        ownDir = System.getenv("user.dir");
        for(String s : args){
            if(s.startsWith("-m=")){
                switch (s.charAt(3)){
                    case 's' :
                    case 'S' :
                        mode = SINGLE_SERVER;
                        break;
                    case 'c' :
                    case 'C' :
                        mode = SINGLE_CLIENT;
                        break;
                    case 'p' :
                    case 'P' :
                    default :
                        mode = PEER;
                        break;
                }
            }
            if(s.startsWith("-d=")){
                ownDir = s.substring(3);
            }
        }
        switch (mode){
            case SINGLE_SERVER:
            case SINGLE_CLIENT:
        }
    }
}
