/*
 * A MySQL Auth Response Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Auth_Response extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Auth.Response");
    
    public long capabilityFlags = MySQL_Flags.CLIENT_PROTOCOL_41;
    public long maxPacketSize = 0;
    public long characterSet = 0;
    public String username = "";
    public String authResponse = "";
    public String schema = "";
    
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
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        if ((this.capabilityFlags & MySQL_Flags.CLIENT_PROTOCOL_41) != 0) {
            payload.add( MySQL_Proto.build_fixed_int(4, this.capabilityFlags));
            payload.add( MySQL_Proto.build_fixed_int(4, this.maxPacketSize));
            payload.add( MySQL_Proto.build_fixed_int(1, this.characterSet));
            payload.add( MySQL_Proto.build_fixed_str(23, ""));
            payload.add( MySQL_Proto.build_null_str(this.username));
            if (this.hasCapabilityFlag(MySQL_Flags.CLIENT_SECURE_CONNECTION))
                payload.add( MySQL_Proto.build_lenenc_str(this.authResponse));
            else
                payload.add( MySQL_Proto.build_null_str(this.authResponse));
            payload.add( MySQL_Proto.build_fixed_str(this.schema.length(), this.schema));
        }
        else {
            payload.add( MySQL_Proto.build_fixed_int(2, this.capabilityFlags));
            payload.add( MySQL_Proto.build_fixed_int(3, this.maxPacketSize));
            payload.add( MySQL_Proto.build_null_str(this.username));
            payload.add( MySQL_Proto.build_null_str(this.authResponse));
        }
        
        return payload;
    }
    
    public static MySQL_Auth_Response loadFromPacket(byte[] packet) {
        Logger.getLogger("MySQL.Auth.Response").trace("loadFromPacket");
        MySQL_Auth_Response obj = new MySQL_Auth_Response();
        int offset = 3;
        
        obj.sequenceId = MySQL_Proto.get_fixed_int(packet, offset, 1);
        offset += MySQL_Proto.get_offset_offset();
        
        obj.capabilityFlags = MySQL_Proto.get_fixed_int(packet, offset, 2);
        MySQL_Proto.get_offset_offset();
        
        if (obj.hasCapabilityFlag(MySQL_Flags.CLIENT_PROTOCOL_41)) {
            obj.capabilityFlags = MySQL_Proto.get_fixed_int(packet, offset, 4);
            offset += MySQL_Proto.get_offset_offset();
            
            obj.maxPacketSize = MySQL_Proto.get_fixed_int(packet, offset, 4);
            offset += MySQL_Proto.get_offset_offset();
            
            obj.characterSet = MySQL_Proto.get_fixed_int(packet, offset, 1);
            offset += MySQL_Proto.get_offset_offset();
            
            MySQL_Proto.get_fixed_str(packet, offset, 23);
            offset += MySQL_Proto.get_offset_offset();
            
            obj.username = MySQL_Proto.get_null_str(packet, offset);
            offset += MySQL_Proto.get_offset_offset();
            
            if (obj.hasCapabilityFlag(MySQL_Flags.CLIENT_SECURE_CONNECTION))
                obj.authResponse = MySQL_Proto.get_null_str(packet, offset);
            else
                obj.authResponse = MySQL_Proto.get_lenenc_str(packet, offset);
            offset += MySQL_Proto.get_offset_offset();
            
            obj.schema = MySQL_Proto.get_eop_str(packet, offset);
            offset += MySQL_Proto.get_offset_offset();
        }
        else {
            obj.capabilityFlags = MySQL_Proto.get_fixed_int(packet, offset, 2);
            offset += MySQL_Proto.get_offset_offset();
            
            obj.maxPacketSize = MySQL_Proto.get_fixed_int(packet, offset, 3);
            offset += MySQL_Proto.get_offset_offset();
            
            obj.username = MySQL_Proto.get_null_str(packet, offset);
            offset += MySQL_Proto.get_offset_offset();
            
            obj.schema = MySQL_Proto.get_null_str(packet, offset);
            offset += MySQL_Proto.get_offset_offset();
        }
        
        return obj;
    }
}
