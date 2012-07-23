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
    
    public void setStatusFlag(long flag) {
        this.statusFlags |= flag;
    }
    
    public void removeStatusFlag(long flag) {
        this.statusFlags &= ~flag;
    }
    
    public void toggleStatusFlag(long flag) {
        this.statusFlags ^= flag;
    }
    
    public ArrayList<byte[]> getPayload() {
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
        
        if ((this.capabilityFlags & MySQL_Flags.CLIENT_SECURE_CONNECTION) != 0) {
            payload.add( MySQL_Proto.build_fixed_str(12, this.challenge2));
            payload.add( MySQL_Proto.build_filler(1));
        }
        
        return payload;
    }
}
