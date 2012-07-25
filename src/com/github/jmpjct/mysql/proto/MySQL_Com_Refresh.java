package com.github.jmpjct.mysql.proto;

/*
 * A MySQL Command Packet
 */

import java.util.ArrayList;
import org.apache.log4j.Logger;

public class MySQL_Com_Refresh extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Refresh");
    
    public long flags = 0x00;
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_REFRESH));
        payload.add(MySQL_Proto.build_fixed_int(1, this.flags));
        
        return payload;
    }
    
    public static MySQL_Com_Refresh loadFromPacket(byte[] packet) {
        Logger.getLogger("MySQL.Com.Refresh").trace("loadFromPacket");
        MySQL_Com_Refresh obj = new MySQL_Com_Refresh();
        MySQL_Proto proto = new MySQL_Proto(packet, 3);
        
        obj.sequenceId = proto.get_fixed_int(1);
        
        // Header
        proto.get_fixed_int(1);
        
        obj.flags = proto.get_fixed_int(1);
        
        return obj;
    }
}
