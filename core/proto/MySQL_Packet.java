/*
 * A MySQL Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public abstract class MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Packet");
    
    public long sequenceId = 0;
    
    public abstract ArrayList<byte[]> getPayload();
    
    public byte[] toPacket() {
        ArrayList<byte[]> payload = this.getPayload();
        
        int size = 0;
        for (byte[] field: payload)
            size += field.length;
        
        byte[] packet = new byte[size+4];
        
        System.arraycopy(MySQL_Proto.build_fixed_int(3, size), 0, packet, 0, 3);
        System.arraycopy(MySQL_Proto.build_fixed_int(1, this.sequenceId), 0, packet, 3, 1);
        
        int offset = 4;
        for (byte[] field: payload) {
            System.arraycopy(field, 0, packet, offset, field.length);
            offset += field.length;
        }
        
        return packet;
    }
    
    public static long getSize(byte[] packet) {
        long size = MySQL_Proto.get_fixed_int(packet, 0, 3);
        MySQL_Proto.get_offset_offset();
        Logger.getLogger("MySQL.Packet").trace("Packet size is "+size);
        return size;
    }
    
    public static byte getType(byte[] packet) {
        return packet[4];
    }
    
    public static long getSequenceId(byte[] packet) {
        return MySQL_Proto.get_fixed_int(packet, 3, 1);
    }
    
}
