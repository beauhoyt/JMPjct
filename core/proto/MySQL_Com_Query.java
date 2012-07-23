/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Query extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Query");
    
    public String query = "";
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_QUERY));
        payload.add(MySQL_Proto.build_fixed_str(this.query.length(), this.query));
        
        return payload;
    }
}
