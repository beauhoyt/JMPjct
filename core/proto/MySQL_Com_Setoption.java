/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Setoption extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Setoption");
    
    public long operation = 0;
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_SET_OPTION));
        payload.add(MySQL_Proto.build_fixed_int(2, this.operation));
        
        return payload;
    }
}
