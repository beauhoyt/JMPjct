/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Statistics extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Statistics");
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_STATISTICS));
        
        return payload;
    }
}
