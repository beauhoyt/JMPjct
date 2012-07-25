package com.github.jmpjct.mysql.proto;

/*
 * A MySQL Command Packet
 */

import java.util.ArrayList;
import org.apache.log4j.Logger;

public class MySQL_Com_Shutdown extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Shutdown");
    
    public long shutdownType = MySQL_Flags.SHUTDOWN_DEFAULT;
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_SHUTDOWN));
        if (this.shutdownType != MySQL_Flags.SHUTDOWN_DEFAULT)
            payload.add(MySQL_Proto.build_fixed_int(1, this.shutdownType));
        
        return payload;
    }
    
    public static MySQL_Com_Shutdown loadFromPacket(byte[] packet) {
        Logger.getLogger("MySQL.Com.Shutdown").trace("loadFromPacket");
        MySQL_Com_Shutdown obj = new MySQL_Com_Shutdown();
        MySQL_Proto proto = new MySQL_Proto(packet, 3);
        
        obj.sequenceId = proto.get_fixed_int(1);
        
        // Header
        proto.get_fixed_int(1);
        
        obj.shutdownType = proto.get_fixed_int(1);
        
        return obj;
    }
    
}
