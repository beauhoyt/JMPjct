import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.commons.io.*;

public class JMTC_Thread extends Thread {
    private Socket socket = null;
    private Socket mysqlSocket = null;
    private String mysqlHost = null;
    private int mysqlPort;
    
    private InputStream clientIn = null;
    private OutputStream clientOut = null;
    
    private InputStream mysqlIn = null;
    private OutputStream mysqlOut = null;
    
    private ArrayList<Integer> buffer = new ArrayList<Integer>();
    
    private int running = 1;

    public JMTC_Thread(Socket socket, String mysqlHost, int mysqlPort) {
        this.socket = socket;
        this.mysqlHost = mysqlHost;
        this.mysqlPort = mysqlPort;
        
        try {
            this.clientIn = this.socket.getInputStream();
            this.clientOut = this.socket.getOutputStream();
        
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
        while (this.running == 1) {
                
            this.readClient();
            this.dumpBuffer();
            this.writeMysql();
                
            this.readMysql();
            this.dumpBuffer();
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
            if (this.buffer.size() > 0)
                System.err.print("Reading from Client "+this.buffer.size()+" bytes.\n");
        }
        catch (IOException e) {
            this.running = 0;
        }
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
            if (this.buffer.size() > 0)
                System.err.print("Reading from MySQL "+this.buffer.size()+" bytes.\n");
        }
        catch (IOException e) {
            this.running = 0;
        }
    }
    
    public void writeClient() {
        int size = this.buffer.size();
        int b = 0;
        int i = 0;
        
        if (size == 0)
            return;
        
        try {
            System.err.print("Writing to client "+this.buffer.size()+" bytes.\n");
            
            for (i = 0; i < size; i++) {
                b = this.buffer.get(i);
                this.clientOut.write(b);
            }
            this.clearBuffer();
        }
        catch (IOException e) {
            this.running = 0;
        }
    }

    public void writeMysql() {
        int size = this.buffer.size();
        int b = 0;
        int i = 0;
        
        if (size == 0)
            return;
        
        try {
            System.err.print("Writing to MySQL "+this.buffer.size()+" bytes.\n");
            
            for (i = 0; i < size; i++) {
                b = this.buffer.get(i);
                this.mysqlOut.write(b);
            }
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
}
