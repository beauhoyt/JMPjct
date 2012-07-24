/*
 * A MySQL Textual Result Set
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_ResultSet_Text {
    public Logger logger = Logger.getLogger("MySQL.ResultSet.Text");
    
    public long sequenceId = 1;
    public static long characterSet = 0;
    
    public ArrayList<MySQL_Column> columns = new ArrayList<MySQL_Column>();
    public ArrayList<MySQL_Row> rows = new ArrayList<MySQL_Row>();
    
    public ArrayList<byte[]> toPackets() {
        this.logger.trace("toPackets");
        ArrayList<byte[]> packets = new ArrayList<byte[]>();
        
        long maxRowSize = 0;
        
        for (MySQL_Column col: this.columns) {
            long size = col.toPacket().length;
            if (size > maxRowSize)
                maxRowSize = size;
        }
        
        maxRowSize = 0;
        
        MySQL_ColCount colCount = new MySQL_ColCount();
        colCount.sequenceId = this.sequenceId;
        this.sequenceId++;
        colCount.colCount = this.columns.size();
        packets.add(colCount.toPacket());
        
        for (MySQL_Column col: this.columns) {
            col.sequenceId = this.sequenceId;
            col.columnLength = maxRowSize;
            this.sequenceId++;
            packets.add(col.toPacket());
        }
        
        MySQL_EOF eof = new MySQL_EOF();
        eof.sequenceId = this.sequenceId;
        this.sequenceId++;
        packets.add(eof.toPacket());
        
        for (MySQL_Row row: this.rows) {
            row.sequenceId = this.sequenceId;
            this.sequenceId++;
            packets.add(row.toPacket());
        }
        
        eof = new MySQL_EOF();
        eof.sequenceId = this.sequenceId;
        this.sequenceId++;
        packets.add(eof.toPacket());
        
        return packets;
    }
    
    public void addColumn(MySQL_Column column) {
        this.columns.add(column);
    }
    
    public void addRow(MySQL_Row row) {
        this.rows.add(row);
    }
}
