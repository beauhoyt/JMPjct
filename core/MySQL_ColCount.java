/*
 * A MySQL Coulmn Count Packet
 */

import org.apache.log4j.Logger;

public class MySQL_ColCount extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.ColCount");
    
    public long colCount = 0;
    
    public byte[] getPayload() {
        int size = 0;
        
        // Column Count
        byte[] colCount = MySQL_Proto.build_lenenc_int(this.colCount);
        size += colCount.length;
        
        byte[] packet = new byte[size];
        
        int offset = 0;
        
        System.arraycopy(colCount, 0, packet, offset, colCount.length);
        offset += colCount.length;
        
        return packet;
    }
}
