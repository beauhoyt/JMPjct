/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Debug extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Debug");
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_DEBUG));
        
        return payload;
    }
}
