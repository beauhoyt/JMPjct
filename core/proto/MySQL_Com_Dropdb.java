/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Dropdb extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Dropdb");
    
    public String schema = "";
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_DROP_DB));
        payload.add(MySQL_Proto.build_fixed_str(this.schema.length(), this.schema));
        
        return payload;
    }
}
