/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Quit extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Quit");
    
    public String query = "";
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_QUIT));
        
        return payload;
    }
}
