/*
 * A MySQL Auth Challenge Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Auth_Challenge extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Auth.Challenge");
    
    public long protocolVersion = 0x0a;
    public String serverVersion = "";
    public long connectionId = 0;
    public String challenge1 = "";
    public long capabilityFlags = MySQL_Flags.CLIENT_PROTOCOL_41;
    public long characterSet = 0;
    public long statusFlags = 0;
    public String challenge2 = "";

    public void setCapabilityFlag(long flag) {
        this.capabilityFlags |= flag;
    }
    
    public void removeCapabilityFlag(long flag) {
        this.capabilityFlags &= ~flag;
    }
    
    public void toggleCapabilityFlag(long flag) {
        this.capabilityFlags ^= flag;
    }
    
    public boolean hasCapabilityFlag(long flag) {
        return ((this.capabilityFlags & flag) == flag);
    }
    
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
        
        payload.add( MySQL_Proto.build_fixed_int(1, this.protocolVersion));
        payload.add( MySQL_Proto.build_null_str(this.serverVersion));
        payload.add( MySQL_Proto.build_fixed_int(4, this.connectionId));
        payload.add( MySQL_Proto.build_fixed_str(8, this.challenge1));
        payload.add( MySQL_Proto.build_filler(1));
        payload.add( MySQL_Proto.build_fixed_int(2, this.capabilityFlags));
        payload.add( MySQL_Proto.build_fixed_int(1, this.characterSet));
        payload.add( MySQL_Proto.build_fixed_int(2, this.statusFlags));
        payload.add( MySQL_Proto.build_fixed_str(13, ""));
        
        if (this.hasCapabilityFlag(MySQL_Flags.CLIENT_SECURE_CONNECTION)) {
            payload.add( MySQL_Proto.build_fixed_str(12, this.challenge2));
            payload.add( MySQL_Proto.build_filler(1));
        }
        
        return payload;
    }
    
    public static MySQL_Auth_Challenge loadFromPacket(byte[] packet) {
        Logger.getLogger("MySQL.Auth.Challenge").trace("loadFromPacket");
        MySQL_Auth_Challenge obj = new MySQL_Auth_Challenge();
        int offset = 3;
        
        obj.sequenceId = MySQL_Proto.get_fixed_int(packet, offset, 1);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.protocolVersion = MySQL_Proto.get_fixed_int(packet, offset, 1);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.serverVersion = MySQL_Proto.get_null_str(packet, offset);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.connectionId = MySQL_Proto.get_fixed_int(packet, offset, 4);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.challenge1 = MySQL_Proto.get_fixed_str(packet, offset, 8);
        offset += MySQL_Proto.get_offset_offset();
        
        MySQL_Proto.get_fixed_str(packet, offset, 1);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.capabilityFlags = MySQL_Proto.get_fixed_int(packet, offset, 2);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.characterSet = MySQL_Proto.get_fixed_int(packet, offset, 1);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.statusFlags = MySQL_Proto.get_fixed_int(packet, offset, 2);
        offset += MySQL_Proto.get_offset_offset();
        
        MySQL_Proto.get_fixed_str(packet, offset, 13);
        offset += MySQL_Proto.get_offset_offset();
        
        if (obj.hasCapabilityFlag(MySQL_Flags.CLIENT_SECURE_CONNECTION)) {
            obj.challenge2 = MySQL_Proto.get_fixed_str(packet, offset, 12);
            offset += MySQL_Proto.get_offset_offset();
            
            MySQL_Proto.get_fixed_str(packet, offset, 1);
            offset += MySQL_Proto.get_offset_offset();
        }
        
        return obj;
    }
}
