/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Query extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Query");
    
    public String query = "";
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_QUERY));
        payload.add(MySQL_Proto.build_fixed_str(this.query.length(), this.query));
        
        return payload;
    }

    public static MySQL_Com_Query loadFromPacket(byte[] packet) {
        Logger.getLogger("MySQL.Com.Query").trace("loadFromPacket");
        MySQL_Com_Query obj = new MySQL_Com_Query();
        int offset = 3;
        
        obj.sequenceId = MySQL_Proto.get_fixed_int(packet, offset, 1);
        offset += MySQL_Proto.get_offset_offset();
        
        // Header
        MySQL_Proto.get_fixed_int(packet, offset, 1);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.query = MySQL_Proto.get_eop_str(packet, offset);
        offset += MySQL_Proto.get_offset_offset();
        
        return obj;
    }
    
}
