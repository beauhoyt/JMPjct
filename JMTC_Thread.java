import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.commons.io.*;

public class JMTC_Thread extends Thread {
    
    // Where to connect to
    private String mysqlHost = null;
    private int mysqlPort;
    
    // MySql server stuff
    private Socket mysqlSocket = null;
    private InputStream mysqlIn = null;
    private OutputStream mysqlOut = null;
    
    // Client stuff
    private Socket clientSocket = null;
    private InputStream clientIn = null;
    private OutputStream clientOut = null;
    
    // Packet Buffer. ArrayList so we can grow/shrink dynamically
    private ArrayList<Integer> buffer = new ArrayList<Integer>();
    private int offset = 0;
    
    // Stop the thread?
    private int running = 1;

    // Connection info
    private int packetType = 0;
    private String schema = "";
    private int sequenceId = 0;
    private String query = "";
    private int affectedRows = 0;
    private int lastInsertId = 0;
    private int statusFlags = 0;
    private int warnings = 0;
    
    // Modes
    private int mode = 0;
    public static final int MODE_AUTH = 0;
    public static final int MODE_COMMAND = 1;
    
    // Packet types
    public static final int COM_QUIT                = 0x01;
    public static final int COM_INIT_DB             = 0x02;
    public static final int COM_QUERY               = 0x03;
    public static final int COM_FIELD_LIST          = 0x04;
    public static final int COM_CREATE_DB           = 0x05;
    public static final int COM_DROP_DB             = 0x06;
    public static final int COM_REFRESH             = 0x07;
    public static final int COM_SHUTDOWN            = 0x08;
    public static final int COM_STATISTICS          = 0x09;
    public static final int COM_PROCESS_INFO        = 0x0a;
    public static final int COM_PROCESS_KILL        = 0x0c;
    public static final int COM_DEBUG               = 0x0d;
    public static final int COM_PING                = 0x0e;
    public static final int COM_CHANGE_USER         = 0x11;
    public static final int COM_BINLOG_DUMP         = 0x12;
    public static final int COM_TABLE_DUMP          = 0x13;
    public static final int COM_CONNECT_OUT         = 0x14;
    public static final int COM_REGISTER_SLAVE      = 0x15;
    public static final int COM_STMT_PREPARE        = 0x16;
    public static final int COM_STMT_EXECUTE        = 0x17;
    public static final int COM_STMT_SEND_LONG_DATA = 0x18;
    public static final int COM_STMT_CLOSE          = 0x19;
    public static final int COM_STMT_RESET          = 0x1a;
    public static final int COM_SET_OPTION          = 0x1b;
    public static final int COM_STMT_FETCH          = 0x1c;
    public static final int COM_UNKNOWN             = 0xff;
    
    public static final int OK  = 0x00;
    public static final int ERR = 0xff;
    
    public static final int SERVER_STATUS_IN_TRANS             = 0x0001;
    public static final int SERVER_STATUS_AUTOCOMMIT           = 0x0002;
    public static final int SERVER_MORE_RESULTS_EXISTS         = 0x0008;
    public static final int SERVER_STATUS_NO_GOOD_INDEX_USED   = 0x0010;
    public static final int SERVER_STATUS_NO_INDEX_USED        = 0x0020;
    public static final int SERVER_STATUS_CURSOR_EXISTS        = 0x0040;
    public static final int SERVER_STATUS_LAST_ROW_SENT        = 0x0080;
    public static final int SERVER_STATUS_DB_DROPPED           = 0x0100;
    public static final int SERVER_STATUS_NO_BACKSLASH_ESCAPES = 0x0200;
    public static final int SERVER_STATUS_METADATA_CHANGED     = 0x0400;
    public static final int SERVER_QUERY_WAS_SLOW              = 0x0800;
    public static final int SERVER_PS_OUT_PARAMS               = 0x1000;
    
    public JMTC_Thread(Socket clientSocket, String mysqlHost, int mysqlPort) {
        this.clientSocket = clientSocket;
        this.mysqlHost = mysqlHost;
        this.mysqlPort = mysqlPort;
        
        try {
            this.clientIn = this.clientSocket.getInputStream();
            this.clientOut = this.clientSocket.getOutputStream();
        
            // Connect to the mysql server on the other side
            this.mysqlSocket = new Socket(this.mysqlHost, this.mysqlPort);
            this.mysqlIn = this.mysqlSocket.getInputStream();
            this.mysqlOut = this.mysqlSocket.getOutputStream();
            System.err.print("Connected to mysql host.\n");
        }
        catch (IOException e) {
            return;
        }
    }

    public void run() {
        
        // Connection init
        this.readClient();
        this.writeMysql();
        
        // Auth Challenge
        this.readMysql();
        this.writeClient();
        
        // Auth Response
        this.readClient();
        this.writeMysql();
        
        // Okay!
        // TODO: Verify this is an OK_PACKET
        this.readMysql();
        this.writeClient();
        
        // Command Phase!
        this.mode = JMTC_Thread.MODE_COMMAND;
        
        while (this.running == 1) {
            
            this.readClient();
            this.writeMysql();
                
            this.readMysql();
            this.writeClient();
            
        }
        System.err.print("Exiting thread.\n");
    }
    
    public void clearBuffer() {
        this.offset = 0;
        this.buffer.clear();
    }

    public void readClient() {
        int b = 0;
        
        try {
            if (this.clientIn.available() == 0)
                Thread.sleep(10);
            
            // Read from the client
            while (this.clientIn.available() > 0) {

                b = this.clientIn.read();
                
                if (b == -1) {
                    this.running = 0;
                    return;
                }
                this.buffer.add(b);
            }
        }
        catch (IOException e) {
            this.running = 0;
        }
        catch (InterruptedException e) {
            this.running = 0;
        }
        this.processClientPacket();
    }

    public void readMysql() {
        int b = 0;
        
        try {
            if (this.mysqlIn.available() == 0)
                Thread.sleep(10);
            
            // Read from the client
            while (this.mysqlIn.available() > 0) {
                
                b = this.mysqlIn.read();
                if (b == -1) {
                    this.running = 0;
                    return;
                }
                this.buffer.add(b);
            }
        }
        catch (IOException e) {
            this.running = 0;
        }
        catch (InterruptedException e) {
            this.running = 0;
        }
        this.processServerPacket();
    }
    
    public void writeClient() {
        int size = this.buffer.size();
        int i = 0;
        
        if (size == 0)
            return;
        
        try {
            System.err.print("Writing to client "+size+" bytes.\n");
            for (i = 0; i < size; i++)
                this.clientOut.write(this.buffer.get(i));
            this.clearBuffer();
        }
        catch (IOException e) {
            this.running = 0;
        }
    }

    public void writeMysql() {
        int size = this.buffer.size();
        int i = 0;
        
        if (size == 0)
            return;
        
        try {
            System.err.print("Writing to MySQL "+size+" bytes.\n");
            for (i = 0; i < size; i++)
                this.mysqlOut.write(this.buffer.get(i));
            this.clearBuffer();
        }
        catch (IOException e) {
            this.running = 0;
        }
    }
    
    public void dumpBuffer() {
        int size = this.buffer.size();
        Integer b = 0;
        int i = 0;
        byte[] buff = new byte[size];
        
        for (i = 0; i < size; i++) {
            b = this.buffer.get(i);
            buff[i] = (byte) (b & 0xFF);
        }
        
        if (size > 0) {
            try {
                HexDump.dump(buff, 0, java.lang.System.err, 0);
            }
            catch (IOException e) {
                return;
            }
        }
    }
    
    public void processClientPacket() {
        if (this.buffer.size() < 4)
            return;
        
        this.getPacketSize();
        this.packetType = this.buffer.get(4);
        this.sequenceId = this.buffer.get(3);
        
        switch (this.packetType) {
            case JMTC_Thread.COM_QUIT:
                System.err.print("-> COM_QUIT\n");
                this.dumpBuffer();
                this.running = 0;
                break;
            
            // Extract out the new default schema
            case JMTC_Thread.COM_INIT_DB:
                this.schema = "";
                for (int i = 5; i < this.buffer.size(); i++)
                    this.schema += (char)this.buffer.get(i).intValue();
                System.err.print("-> USE "+this.schema+"\n");
                break;
            
            // Query
            case JMTC_Thread.COM_QUERY:
                this.query = "";
                for (int i = 5; i < this.buffer.size(); i++)
                    this.query += (char)this.buffer.get(i).intValue();
                System.err.print("-> "+this.query+"\n");
                break;
            
            default:
                System.err.print("Packet is "+this.packetType+" type.\n");
                this.dumpBuffer();
                break;
        }
    }
    
    public void processServerPacket() {
        if (this.buffer.size() < 4)
            return;
        
        this.getPacketSize();
        this.packetType = this.buffer.get(4);
        this.sequenceId = this.buffer.get(3);
        
        switch (this.packetType) {
            case JMTC_Thread.OK:
                
                if (this.mode == JMTC_Thread.MODE_COMMAND) {
                    this.offset = 5;
                    this.affectedRows = this.get_lenenc_int();
                    this.lastInsertId = this.get_lenenc_int();
                    this.statusFlags  = this.get_fixed_int(2);
                    this.warnings     = this.get_fixed_int(2);
                }
                
                System.err.print("<- OK");
                if (this.affectedRows > 0)
                    System.err.print(" Affected rows: "+this.affectedRows);
                if (this.lastInsertId > 0)
                    System.err.print(" Inserted id: "+this.lastInsertId);
                if (this.warnings > 0)
                    System.err.print(" Warnings: "+this.warnings);
                if (this.statusFlags > 0) {
                    if ((this.statusFlags & JMTC_Thread.SERVER_STATUS_IN_TRANS) != 0)
                        System.err.print(" SERVER_STATUS_IN_TRANS");
                    if ((this.statusFlags & JMTC_Thread.SERVER_STATUS_AUTOCOMMIT) != 0)
                        System.err.print(" SERVER_STATUS_AUTOCOMMIT");
                    if ((this.statusFlags & JMTC_Thread.SERVER_MORE_RESULTS_EXISTS) != 0)
                        System.err.print(" SERVER_MORE_RESULTS_EXISTS");
                    if ((this.statusFlags & JMTC_Thread.SERVER_STATUS_NO_GOOD_INDEX_USED) != 0)
                        System.err.print(" SERVER_STATUS_NO_GOOD_INDEX_USED");
                    if ((this.statusFlags & JMTC_Thread.SERVER_STATUS_NO_INDEX_USED) != 0)
                        System.err.print(" SERVER_STATUS_NO_INDEX_USED");
                    if ((this.statusFlags & JMTC_Thread.SERVER_STATUS_CURSOR_EXISTS) != 0)
                        System.err.print(" SERVER_STATUS_CURSOR_EXISTS");
                    if ((this.statusFlags & JMTC_Thread.SERVER_STATUS_LAST_ROW_SENT) != 0)
                        System.err.print(" SERVER_STATUS_LAST_ROW_SENT");
                    if ((this.statusFlags & JMTC_Thread.SERVER_STATUS_LAST_ROW_SENT) != 0)
                        System.err.print(" SERVER_STATUS_LAST_ROW_SENT");
                    if ((this.statusFlags & JMTC_Thread.SERVER_STATUS_DB_DROPPED) != 0)
                        System.err.print(" SERVER_STATUS_DB_DROPPED");
                    if ((this.statusFlags & JMTC_Thread.SERVER_STATUS_NO_BACKSLASH_ESCAPES) != 0)
                        System.err.print(" SERVER_STATUS_NO_BACKSLASH_ESCAPES");
                    if ((this.statusFlags & JMTC_Thread.SERVER_STATUS_METADATA_CHANGED) != 0)
                        System.err.print(" SERVER_STATUS_METADATA_CHANGED");
                    if ((this.statusFlags & JMTC_Thread.SERVER_QUERY_WAS_SLOW) != 0)
                        System.err.print(" SERVER_QUERY_WAS_SLOW");
                    if ((this.statusFlags & JMTC_Thread.SERVER_PS_OUT_PARAMS) != 0)
                        System.err.print(" SERVER_PS_OUT_PARAMS");
                }
                
                System.err.print("\n");
                break;
            
            // Extract out the new default schema
            case JMTC_Thread.ERR:
                System.err.print("<- ERR\n");
                break;
            
            default:
                System.err.print("Packet is "+this.packetType+" type.\n");
                this.dumpBuffer();
                break;
        }
    }
    
    public int getPacketSize() {
        int size = 0;
        int offset = this.offset;
        this.offset = 0;
        size = this.get_fixed_int(3);
        this.offset = offset;
        return size;
    }
    
    public int get_lenenc_int() {
        int value = -1;
        
        // 1 byte int
        if (this.buffer.get(this.offset) < 251 && this.buffer.size() >= (1 + this.offset) ) {
            value = this.buffer.get(this.offset);
            this.offset += 1;
            return value;
        }
            
        // 2 byte int
        if (this.buffer.get(this.offset) == 252 && this.buffer.size() >= (3 + this.offset) ) {
            value = (this.buffer.get(this.offset+1) << 0)
                  | (this.buffer.get(this.offset+2) << 8);
                  
            this.offset += 3;
            return value;
        }
        
        // 3 byte int
        if (this.buffer.get(this.offset) == 253 && this.buffer.size() >= (4 + this.offset) ) {
            value = (this.buffer.get(this.offset+1) << 0)
                  | (this.buffer.get(this.offset+2) << 8)
                  | (this.buffer.get(this.offset+3) << 16);
                  
            this.offset += 4;
            return value;
        }
        
        // 8 byte int
        if (this.buffer.get(this.offset) == 254  && this.buffer.size() >= (9 + this.offset) ) {
            value = (this.buffer.get(this.offset+5) << 0)
                  | (this.buffer.get(this.offset+6) << 8)
                  | (this.buffer.get(this.offset+7) << 16)
                  | (this.buffer.get(this.offset+8) << 24);
                  
            value = value << 32;
                  
            value |= (this.buffer.get(this.offset+1) << 0)
                  |  (this.buffer.get(this.offset+2) << 8)
                  |  (this.buffer.get(this.offset+3) << 16)
                  |  (this.buffer.get(this.offset+3) << 24);

            this.offset += 9;
            return value;
        }
        
        System.err.print("Decoding int at offset "+this.offset+" failed!");
        this.dumpBuffer();
        
        return -1;
    }
    
    public int get_fixed_int(int size) {
        int value = -1;
        
        // 1 byte int
        if (size == 1 && this.buffer.size() >= (size + this.offset) ) {
            value = this.buffer.get(this.offset);
            this.offset += 1;
            return value;
        }
            
        // 2 byte int
        if (size == 2 && this.buffer.size() >= (size + this.offset) ) {
            value = (this.buffer.get(this.offset+0) << 0)
                  | (this.buffer.get(this.offset+1) << 8);
            this.offset += 2;
            return value;
        }
        
        // 3 byte int
        if (size == 3 && this.buffer.size() >= (size + this.offset) ) {
            value = (this.buffer.get(this.offset+0) << 0)
                  | (this.buffer.get(this.offset+1) << 8)
                  | (this.buffer.get(this.offset+2) << 16);
            this.offset += 3;
            return value;
        }
        
        // 8 byte int
        if (size == 8 && this.buffer.size() >= (size + this.offset) ) {
            value = (this.buffer.get(this.offset+4) << 0)
                  | (this.buffer.get(this.offset+5) << 8)
                  | (this.buffer.get(this.offset+6) << 16)
                  | (this.buffer.get(this.offset+7) << 24);
                  
            value = value << 32;
                  
            value |= (this.buffer.get(this.offset+0) << 0)
                  |  (this.buffer.get(this.offset+1) << 8)
                  |  (this.buffer.get(this.offset+2) << 16)
                  |  (this.buffer.get(this.offset+3) << 24);
                  
            this.offset += 8;
            return value;
        }
        
        System.err.print("Decoding int at offset "+this.offset+" failed!\n");
        this.dumpBuffer();
        
        return -1;
    }
    
}
