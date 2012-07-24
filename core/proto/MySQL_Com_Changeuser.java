/*
 * A MySQL Command Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Com_Changeuser extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Changeuser");
    
    public String user = "";
    public String authResponse = "";
    public String schema = "";
    public long characterSet = 0;
    public long capabilities = 0;
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_CHANGE_USER));
        payload.add(MySQL_Proto.build_null_str(this.user));
        if (this.capabilities != MySQL_Flags.CLIENT_SECURE_CONNECTION)
            payload.add(MySQL_Proto.build_lenenc_str(this.authResponse));
        else
            payload.add(MySQL_Proto.build_null_str(this.authResponse));
        payload.add(MySQL_Proto.build_null_str(this.schema));
        payload.add(MySQL_Proto.build_fixed_int(2, this.characterSet));
        
        return payload;
    }
}
