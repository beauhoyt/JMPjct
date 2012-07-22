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
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.EOF));
        payload.add(MySQL_Proto.build_fixed_int(2, this.statusFlags));
        payload.add(MySQL_Proto.build_fixed_int(2, this.warnings));
        
        return payload;
    }
}
