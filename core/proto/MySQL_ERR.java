/*
 * A MySQL ERR Packet
 *
 * https://dev.mysql.com/doc/refman/5.5/en/error-messages-server.html
 * https://dev.mysql.com/doc/refman/5.5/en/error-messages-client.html
 * 
 */

import java.util.ArrayList;
import org.apache.log4j.Logger;

public class MySQL_ERR extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.ERR");
    
    public long errorCode = 0;
    public String sqlState = "HY000";
    public String errorMessage = "";
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.ERR));
        payload.add(MySQL_Proto.build_fixed_int(2, this.errorCode));
        payload.add(MySQL_Proto.build_byte((byte)'#'));
        payload.add(MySQL_Proto.build_fixed_str(5, this.sqlState));
        payload.add(MySQL_Proto.build_fixed_str(this.errorMessage.length(), this.errorMessage));
        
        return payload;
    }
}
