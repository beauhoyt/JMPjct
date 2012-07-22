/*
 * A MySQL Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public abstract class MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL_Packet");
    
    public long sequenceId = 0;
    
    public abstract byte[] getPayload();
    
    public byte[] toPacket() {
        byte[] data = this.getPayload();
        byte[] packet = new byte[data.length+4];
        byte[] packetSize = MySQL_Proto.build_fixed_int(3, data.length);
        
        System.arraycopy(MySQL_Proto.build_fixed_int(3, data.length), 0, packet, 0, 3);
        System.arraycopy(MySQL_Proto.build_fixed_int(1, this.sequenceId), 0, packet, 3, 1);
        System.arraycopy(data, 0, packet, 4, data.length);
        
        return packet;
    }
}
