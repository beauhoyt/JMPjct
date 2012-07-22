/*
 * A MySQL Coulmn Count Packet
 */

import java.util.ArrayList;
import org.apache.log4j.Logger;

public class MySQL_ColCount extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.ColCount");
    
    public long colCount = 0;
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_lenenc_int(this.colCount));
        
        return payload;
    }
}
