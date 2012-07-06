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
        int readBytes = 0;
        byte[] data = new byte[1024];
        
        try {
            // Read from the client
            while (this.clientIn.available() > 0) {
                readBytes = this.clientIn.read(data, 0, 1024);
                if (readBytes == -1) {
                    this.running = 0;
                    return;
                }
                System.err.print("Reading from Client "+readBytes+" bytes.\n");
                this.buffer.addAll(Arrays.asList(data));
            }
        }
        catch (IOException e) {
            this.running = 0;
        }
        this.processClientPacket();
    }

    public void readMysql() {
        int readBytes = 0;
        byte[] data = new byte[1024];
        
        try {
            // Read from the client
            while (this.mysqlIn.available() > 0) {
                readBytes = this.mysqlIn.read(data, 0, 1024);
                if (readBytes == -1) {
                    this.running = 0;
                    return;
                }
                System.err.print("Reading from MySQL "+readBytes+" bytes.\n");
                this.buffer.addAll(Arrays.asList(data));
            }
        }
        catch (IOException e) {
            this.running = 0;
        }
        this.processClientPacket();
    }
    
    public void writeClient() {
        int size = this.buffer.size();
        
        if (size == 0)
            return;
        
        try {
            System.err.print("Writing to client "+size+" bytes.\n");
            this.clientOut.write(this.buffer.toArray());
            this.clearBuffer();
        }
        catch (IOException e) {
            this.running = 0;
        }
    }

    public void writeMysql() {
        int size = this.buffer.size();
        
        if (size == 0)
            return;
        
        try {
            System.err.print("Writing to MySQL "+size+" bytes.\n");
            this.mysqlOut.write(this.buffer.toArray());
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
        this.dumpBuffer();
        int size = this.buffer.size();
        int packetSize = 0;
        int packetType = 0;
        
        if (size < 4)
            return;
        
        this.getPacketSize();
        
        packetType = this.buffer.get(3);
        
        System.err.print("Packet is "+packetType+" type.\n");
    }
    
    public void processServerPacket() {
        int type = this.buffer.get(4);
        
        // Set to unknown
        this.packetType = this.COM_UNKNOWN;
        
        if (type == this.COM_INIT_DB) {
            this.packetType = this.COM_INIT_DB
            
        }
        
        
        this.dumpBuffer();
    }
    
    public int getPacketSize() {
        int size = 0;
        size = size + this.buffer.get(0);
        System.err.print("Packet is "+packetSize+" bytes.\n");
        
        size = size + this.buffer.get(1);
        System.err.print("Packet is "+packetSize+" bytes.\n");
        
        size = size + this.buffer.get(2);
        System.err.print("Packet is "+packetSize+" bytes.\n");
        
        return size;
    }
    
    public int get_lenenc_int(int offset) {
        int value = 0;
        int b = 0;
        
        
        return value;
    }
    
}
