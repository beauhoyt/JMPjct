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
    public String database = "";
    
    public void setCapabilityFlag(long flag) {
        this.capabilityFlags |= flag;
    }
    
    public void removeCapabilityFlag(long flag) {
        this.capabilityFlags &= ~flag;
    }
    
    public void toggleCapabilityFlag(long flag) {
        this.capabilityFlags ^= flag;
    }
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        if ((this.capabilityFlags & MySQL_Flags.CLIENT_PROTOCOL_41) != 0) {
            payload.add( MySQL_Proto.build_fixed_int(4, this.capabilityFlags));
            payload.add( MySQL_Proto.build_fixed_int(4, this.maxPacketSize));
            payload.add( MySQL_Proto.build_fixed_int(1, this.characterSet));
            payload.add( MySQL_Proto.build_fixed_str(23, ""));
            payload.add( MySQL_Proto.build_null_str(this.username));
            if ((this.capabilityFlags & MySQL_Flags.CLIENT_SECURE_CONNECTION) != 0)
                payload.add( MySQL_Proto.build_lenenc_str(this.authResponse));
            else
                payload.add( MySQL_Proto.build_null_str(this.authResponse));
            payload.add( MySQL_Proto.build_fixed_str(this.database.length(), this.database));
        }
        else {
            payload.add( MySQL_Proto.build_fixed_int(2, this.capabilityFlags));
            payload.add( MySQL_Proto.build_fixed_int(3, this.maxPacketSize));
            payload.add( MySQL_Proto.build_null_str(this.username));
            payload.add( MySQL_Proto.build_null_str(this.authResponse));
        }
        
        return payload;
    }
}
