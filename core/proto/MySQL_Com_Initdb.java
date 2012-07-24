/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Initdb extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Initdb");
    
    public String schema = "";
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_INIT_DB));
        payload.add(MySQL_Proto.build_fixed_str(this.schema.length(), this.schema));
        
        return payload;
    }
    
    public static MySQL_Com_Initdb loadFromPacket(byte[] packet) {
        Logger.getLogger("MySQL.Com.Initdb").trace("loadFromPacket");
        MySQL_Com_Initdb obj = new MySQL_Com_Initdb();
        MySQL_Proto proto = new MySQL_Proto(packet, 3);
        
        obj.sequenceId = proto.get_fixed_int(1);
        
        // Header
        proto.get_fixed_int(1);
        
        obj.schema = proto.get_eop_str();
        
        return obj;
    }
    
}
