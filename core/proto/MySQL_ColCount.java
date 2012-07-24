/*
 * A MySQL Coulmn Count Packet
 */

import java.util.ArrayList;
import org.apache.log4j.Logger;

public class MySQL_ColCount extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.ColCount");
    
    public long colCount = 0;
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_lenenc_int(this.colCount));
        
        return payload;
    }
    
    public static MySQL_ColCount loadFromPacket(byte[] packet) {
        Logger.getLogger("MySQL.ColCount").trace("loadFromPacket");
        MySQL_ColCount obj = new MySQL_ColCount();
        MySQL_Proto proto = new MySQL_Proto(packet, 3);
        
        obj.sequenceId = proto.get_fixed_int(1);
        obj.colCount = proto.get_lenenc_int();
        
        return obj;
    }
    
}
