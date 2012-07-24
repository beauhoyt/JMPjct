/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Initdb extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Initdb");
    
    public String schema = "";
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_INIT_DB));
        payload.add(MySQL_Proto.build_fixed_str(this.schema.length(), this.schema));
        
        return payload;
    }
    
    public static MySQL_Com_Initdb loadFromPacket(byte[] packet) {
        MySQL_Com_Initdb obj = new MySQL_Com_Initdb();
        int offset = 3;
        
        obj.sequenceId = MySQL_Proto.get_fixed_int(packet, offset, 1);
        offset += MySQL_Proto.get_offset_offset();
        
        // Header
        MySQL_Proto.get_fixed_int(packet, offset, 1);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.schema = MySQL_Proto.get_eop_str(packet, offset);
        offset += MySQL_Proto.get_offset_offset();
        
        return obj;
    }
    
}
