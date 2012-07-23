/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Refresh extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Refresh");
    
    public long flags = 0x00;
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_REFRESH));
        payload.add(MySQL_Proto.build_fixed_int(1, this.flags));
        
        return payload;
    }
}
