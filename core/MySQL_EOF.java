/*
 * A MySQL EOF Packet
 */

import org.apache.log4j.Logger;

public class MySQL_EOF extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL_EOF");
    
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
    
    public byte[] getPayload() {
        int size = 0;
        
        // 1 byte for the EOF header
        byte[] header = new byte[1];
        header[0] = MySQL_Flags.EOF;
        size += header.length;

        // status flags
        byte[] statusFlags = MySQL_Proto.build_fixed_int(2, this.statusFlags);
        size += statusFlags.length;
        
        // Warnings
        byte[] warnings = MySQL_Proto.build_fixed_int(2, this.warnings);
        size += warnings.length;
        
        byte[] packet = new byte[size];
        
        int offset = 0;
        
        System.arraycopy(header, 0, packet, offset, header.length);
        offset += header.length;
        
        System.arraycopy(statusFlags, 0, packet, offset, statusFlags.length);
        offset += statusFlags.length;
        
        System.arraycopy(warnings, 0, packet, offset, warnings.length);
        offset += warnings.length;
        
        return packet;
    }
}
