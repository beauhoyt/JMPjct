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
        MySQL_Proto proto = new MySQL_Proto(packet, 3);
        
        obj.sequenceId = proto.get_fixed_int(1);
        obj.capabilityFlags = proto.get_fixed_int(2);
        proto.offset -= 2;
        
        if (obj.hasCapabilityFlag(MySQL_Flags.CLIENT_PROTOCOL_41)) {
            obj.capabilityFlags = proto.get_fixed_int(4);
            obj.maxPacketSize = proto.get_fixed_int(4);
            obj.characterSet = proto.get_fixed_int(1);
            proto.get_fixed_str(23);
            obj.username = proto.get_null_str();
            
            if (obj.hasCapabilityFlag(MySQL_Flags.CLIENT_SECURE_CONNECTION))
                obj.authResponse = proto.get_null_str();
            else
                obj.authResponse = proto.get_lenenc_str();
            
            obj.schema = proto.get_eop_str();
        }
        else {
            obj.capabilityFlags = proto.get_fixed_int(2);
            obj.maxPacketSize = proto.get_fixed_int(3);
            obj.username = proto.get_null_str();
            obj.schema = proto.get_null_str();
        }
        
        return obj;
    }
}
