/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Processkill extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Processkill");
    
    public long connectionId = 0;
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_PROCESS_KILL));
        payload.add(MySQL_Proto.build_fixed_int(4, this.connectionId));
        
        return payload;
    }
}
