/*
 * A MySQL OK Packet
 */

/*

OK packet
---------

::

  OK
    
    direction: server -> client

    payload:
      1              [00] the OK header
      lenenc-int     affected rows
      lenenc-int     last-insert-id
      2              status flags
        if capabilities & PROTOCOL_41:
      2              warnings 

    example:
      07 00 00 02 00 00 00 02    00 00 00                   ...........     

Status Flags
............
 
The status flags are a bit-field:

====== =============
flag   constant name
====== =============
0x0001 SERVER_STATUS_IN_TRANS
0x0002 SERVER_STATUS_AUTOCOMMIT
0x0008 _`SERVER_MORE_RESULTS_EXISTS`
0x0010 SERVER_STATUS_NO_GOOD_INDEX_USED
0x0020 SERVER_STATUS_NO_INDEX_USED
0x0040 SERVER_STATUS_CURSOR_EXISTS
0x0080 SERVER_STATUS_LAST_ROW_SENT
0x0100 SERVER_STATUS_DB_DROPPED
0x0200 SERVER_STATUS_NO_BACKSLASH_ESCAPES
0x0400 SERVER_STATUS_METADATA_CHANGED
0x0800 SERVER_QUERY_WAS_SLOW
0x1000 SERVER_PS_OUT_PARAMS
====== =============

*/


import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_OK {
    public Logger logger = Logger.getLogger("MySQL_OK");
    
    public long sequenceId = 0;
    public long affectedRows = 0;
    public long lastInsertId = 0;
    public long statusFlags = 0;
    public long warnings = 0;
    
    public MySQL_OK() {
        return;
    }
    
    public void setStatusFlag(long flag) {
        this.statusFlags |= flag;
    }
    
    public void removeStatusFlag(long flag) {
        this.statusFlags &= ~flag;
    }
    
    public void toggleStatusFlag(long flag) {
        this.statusFlags ^= flag;
    }
    
    public byte[] toPacket() {
        int size = 0;
        
        // 3 bytes for the length
        size += 3;
        
        // sequenceId
        byte[] sequenceId = MySQL_Proto.build_fixed_int(1, this.sequenceId);
        size += sequenceId.length;
        
        // 1 byte for the OK header
        byte[] header = new byte[1];
        header[0] = MySQL_Flags.OK;
        size += header.length;
        
        // Affected Rows
        byte[] affectedRows = MySQL_Proto.build_lenenc_int(this.affectedRows);
        size += affectedRows.length;
        
        // last-insert-id
        byte[] lastInsertId = MySQL_Proto.build_lenenc_int(this.lastInsertId);
        size += lastInsertId.length;
        
        // status flags
        byte[] statusFlags = MySQL_Proto.build_fixed_int(2, this.statusFlags);
        size += statusFlags.length;
        
        // Warnings
        byte[] warnings = MySQL_Proto.build_fixed_int(2, this.warnings);
        size += warnings.length;
        
        byte[] packet = new byte[size];
        byte[] packetSize = MySQL_Proto.build_fixed_int(3, (size - 4));
        
        int offset = 0;
        
        System.arraycopy(packetSize, 0, packet, offset, packetSize.length);
        offset += packetSize.length;
        
        System.arraycopy(sequenceId, 0, packet, offset, sequenceId.length);
        offset += sequenceId.length;
        
        System.arraycopy(header, 0, packet, offset, header.length);
        offset += header.length;
        
        System.arraycopy(affectedRows, 0, packet, offset, affectedRows.length);
        offset += affectedRows.length;
        
        System.arraycopy(lastInsertId, 0, packet, offset, lastInsertId.length);
        offset += lastInsertId.length;
        
        System.arraycopy(statusFlags, 0, packet, offset, statusFlags.length);
        offset += statusFlags.length;
        
        System.arraycopy(warnings, 0, packet, offset, warnings.length);
        offset += warnings.length;
        
        return packet;
    }
    
}
