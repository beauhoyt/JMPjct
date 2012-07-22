/*
 * A MySQL ERR Packet
 *
 * https://dev.mysql.com/doc/refman/5.5/en/error-messages-server.html
 * https://dev.mysql.com/doc/refman/5.5/en/error-messages-client.html
 * 
 */

/*

ERR packet
----------

::

  ERR
    
    direction: server -> client

    payload:
      1              [ff] the ERR header
      2              error code 
        if capabilities & PROTOCOL_41:
      1              '#' the sql-state marker
      string[5]      sql-state
        all protocols:
      string[p]      error-message

    example:
      17 00 00 01 ff 48 04 23    48 59 30 30 30 4e 6f 20    .....H.#HY000No 
      74 61 62 6c 65 73 20 75    73 65 64                   tables used 

*/


import org.apache.log4j.Logger;

public class MySQL_ERR extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL_ERR");
    
    public long errorCode = 0;
    public String sqlState = "HY000";
    public String errorMessage = "";
    
    public byte[] getPayload() {
        int size = 0;
        
        // 1 byte for the ERR header
        byte[] header = new byte[1];
        header[0] = MySQL_Flags.ERR;
        size += header.length;

        // error code
        byte[] errorCode = MySQL_Proto.build_fixed_int(2, this.errorCode);
        size += errorCode.length;
        
        // 1 byte for the sql-state marker
        byte[] sqlStateMarker = new byte[1];
        sqlStateMarker[0] = (byte)'#';
        size += sqlStateMarker.length;
        
        // sql-state
        byte[] sqlState = MySQL_Proto.build_fixed_str(this.sqlState, 5);
        size += sqlState.length;
        
        // error-message
        byte[] errorMessage = MySQL_Proto.build_fixed_str(this.errorMessage, this.errorMessage.length());
        size += errorMessage.length;
        
        byte[] packet = new byte[size];
        
        int offset = 0;
        
        System.arraycopy(header, 0, packet, offset, header.length);
        offset += header.length;
        
        System.arraycopy(errorCode, 0, packet, offset, errorCode.length);
        offset += errorCode.length;
        
        System.arraycopy(sqlStateMarker, 0, packet, offset, sqlStateMarker.length);
        offset += sqlStateMarker.length;
        
        System.arraycopy(sqlState, 0, packet, offset, sqlState.length);
        offset += sqlState.length;
        
        System.arraycopy(errorMessage, 0, packet, offset, errorMessage.length);
        offset += errorMessage.length;
        
        return packet;
    }
    
}
