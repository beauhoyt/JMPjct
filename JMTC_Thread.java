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
    
    // Stop the thread?
    private int running = 1;

    // What packet type is this?
    private int packetType = 0;
    private String schema = "";
    private int sequenceId = 0;
    private String query = "";
    
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
        
        while (this.running == 1) {
            
            this.readClient();
            this.writeMysql();
                
            this.readMysql();
            this.writeClient();
            
        }
        System.err.print("Exiting thread.\n");
    }
    
    public void clearBuffer() {
        this.buffer.clear();
    }

    public void readClient() {
        int b = 0;
        
        try {
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
        this.processClientPacket();
    }

    public void readMysql() {
        int b = 0;
        
        try {
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
            // COM_QUIT
            case 0x01:
                System.err.print("-> COM_QUIT\n");
                this.dumpBuffer();
                this.running = 0;
                break;
            
            // Extract out the new default schema
            case 0x02:
                this.schema = "";
                for (int i = 5; i < this.buffer.size(); i++)
                    this.schema += (char)this.buffer.get(i).intValue();
                System.err.print("-> USE "+this.schema+"\n");
                break;
            
            // Query
            case 0x03:
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
        this.dumpBuffer();
    }
    
    public int getPacketSize() {
        int size = 0;
        
        System.err.print("Buffer is "+this.buffer.size()+" bytes.\n");
        
        size = size + this.buffer.get(0);
        
        /*
        System.err.print("Packet is "+size+" bytes.\n");
        
        size = size + this.buffer.get(1);
        System.err.print("Packet is "+size+" bytes.\n");
        
        size = size + this.buffer.get(2);
        System.err.print("Packet is "+size+" bytes.\n");
        */
        
        return size;
    }
    
    public int get_lenenc_int(int offset) {
        int value = 0;
        int b = 0;
        
        
        return value;
    }
    
}
