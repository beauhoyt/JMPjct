/*
 * A MySQL OK Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_OK extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.OK");
    
    public long affectedRows = 0;
    public long lastInsertId = 0;
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
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.OK));
        payload.add(MySQL_Proto.build_lenenc_int(this.affectedRows));
        payload.add(MySQL_Proto.build_lenenc_int(this.lastInsertId));
        payload.add(MySQL_Proto.build_fixed_int(2, this.statusFlags));
        payload.add(MySQL_Proto.build_fixed_int(2, this.warnings));
        
        return payload;
    }
}
