/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Shutdown extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Shutdown");
    
    public long shutdownType = MySQL_Flags.SHUTDOWN_DEFAULT;
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_SHUTDOWN));
        if (this.shutdownType != MySQL_Flags.SHUTDOWN_DEFAULT)
            payload.add(MySQL_Proto.build_fixed_int(1, this.shutdownType));
        
        return payload;
    }
}
