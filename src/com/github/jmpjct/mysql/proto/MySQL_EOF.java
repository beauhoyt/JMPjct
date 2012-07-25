package com.github.jmpjct.mysql.proto;

/*
 * A MySQL EOF Packet
 */

import java.util.ArrayList;
import org.apache.log4j.Logger;

public class MySQL_EOF extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.EOF");
    
    public long statusFlags = 0;
    public long warnings = 0;
    
    public void setStatusFlag(long flag) {
        this.statusFlags |= flag;
    }
    
    public void removeStatusFlag(long flag) {
        this.statusFlags &= ~flag;
    }
    
    public void toggleStatusFlag(long flag) {
        this.statusFlags ^= flag;
    }
    
    public boolean hasStatusFlag(long flag) {
        return ((this.statusFlags & flag) == flag);
    }
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.EOF));
        payload.add(MySQL_Proto.build_fixed_int(2, this.statusFlags));
        payload.add(MySQL_Proto.build_fixed_int(2, this.warnings));
        
        return payload;
    }
    
    public static MySQL_EOF loadFromPacket(byte[] packet) {
        Logger.getLogger("MySQL.EOF").trace("loadFromPacket");
        MySQL_EOF obj = new MySQL_EOF();
        MySQL_Proto proto = new MySQL_Proto(packet, 3);
        
        obj.sequenceId = proto.get_fixed_int(1);
        
        // Header
        proto.get_fixed_int(1);
        
        obj.statusFlags = proto.get_fixed_int(2);
        
        obj.warnings = proto.get_fixed_int(2);
        
        return obj;
    }
}
