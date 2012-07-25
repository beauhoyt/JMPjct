package com.github.jmpjct.mysql.proto;

/*
 * A MySQL Command Packet
 */

import java.util.ArrayList;
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
        MySQL_Proto proto = new MySQL_Proto(packet, 3);
        
        obj.sequenceId = proto.get_fixed_int(1);
        
        // Header
        proto.get_fixed_int(1);
        
        obj.query = proto.get_eop_str();
        
        return obj;
    }
    
}
