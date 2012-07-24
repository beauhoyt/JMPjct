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
        int offset = 3;
        
        obj.sequenceId = MySQL_Proto.get_fixed_int(packet, offset, 1);
        offset += MySQL_Proto.get_offset_offset();
        
        // Header
        MySQL_Proto.get_fixed_int(packet, offset, 1);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.statusFlags = MySQL_Proto.get_fixed_int(packet, offset, 2);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.warnings = MySQL_Proto.get_fixed_int(packet, offset, 2);
        offset += MySQL_Proto.get_offset_offset();
        
        return obj;
    }
}
