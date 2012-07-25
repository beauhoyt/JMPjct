package com.github.jmpjct.mysql.proto;

/*
 * A MySQL Command Packet
 */

import java.util.ArrayList;
import org.apache.log4j.Logger;

public class MySQL_Com_Quit extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Quit");
    
    public String query = "";
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_QUIT));
        
        return payload;
    }
    
    public static MySQL_Com_Quit loadFromPacket(byte[] packet) {
        Logger.getLogger("MySQL.Com.Quit").trace("loadFromPacket");
        MySQL_Com_Quit obj = new MySQL_Com_Quit();
        MySQL_Proto proto = new MySQL_Proto(packet, 3);
        
        obj.sequenceId = proto.get_fixed_int(1);
        
        // Header
        proto.get_fixed_int(1);
        
        return obj;
    }
}
