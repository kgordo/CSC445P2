package codes;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class ERRORCODES {

    public static final short UNDEFINED = 0;
    public static final short FILENOTFOUND = 1;
    public static final short ACCESSVIOLATION = 2;
    public static final short DISKFULL = 3;
    public static final short ILLEGALOP = 4;
    public static final short UNKOWNTID = 5;
    public static final short FILEEXISTS = 6;
    public static final short NOSUCHUSER = 7;

    public static final Map<Short, String> ERRORMESSAGES = new HashMap<>(){
        {
            put(UNDEFINED, "Unknown Error");
            put(FILENOTFOUND, "File Not Found");
            put(ACCESSVIOLATION, "Access Violation");
            put(DISKFULL, "Disk Full or Allocation Exceeded");
            put(ILLEGALOP, "Illegal TFTP Operation");
            put(UNKOWNTID, "Unknown TID");
            put(FILEEXISTS, "File Already Exists");
            put(NOSUCHUSER, "No Such User");
        }
    };

}
