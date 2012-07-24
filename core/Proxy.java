/*
 * Base proxy code. This should really just move data back and forth
 * Calling plugins as needed
 */

import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

public class Proxy extends Thread {
    public Logger logger = Logger.getLogger("Proxy");
    
    public Socket clientSocket = null;
    public InputStream clientIn = null;
    public OutputStream clientOut = null;
    
    // Plugins
    public ArrayList<Proxy_Plugin> plugins = new ArrayList<Proxy_Plugin>();
    
    // Packet Buffer. ArrayList so we can grow/shrink dynamically
    public ArrayList<byte[]> buffer = new ArrayList<byte[]>();
    public int packet_id = 0;
    public int offset = 0;
    
    // Stop the thread?
    public boolean running = true;

    // What sorta of result set should we expect?
    public int expectedResultSet = MySQL_Flags.RS_OK;
    
    // Connection info
    MySQL_Auth_Challenge authChallenge = null;
    MySQL_Auth_Response authReply = null;
    
    public String schema = "";
    public String query = "";
    public long statusFlags = 0;
    public long sequenceId = 0;
    
    // Buffer or directly pass though the data
    public boolean bufferResultSet = false;
    
    // Modes
    public int mode = MySQL_Flags.MODE_INIT;
    
    // Allow plugins to muck with the modes
    public int nextMode = MySQL_Flags.MODE_INIT;
    
    public Proxy(Socket clientSocket, String mysqlHost, int mysqlPort, ArrayList<Proxy_Plugin> plugins) {
        this.clientSocket = clientSocket;
        this.plugins = plugins;
        
        try {
            this.clientIn = this.clientSocket.getInputStream();
            this.clientOut = this.clientSocket.getOutputStream();
        }
        catch (IOException e) {
            this.logger.fatal("IOException: "+e);
            this.running = false;
            return;
        }
    }

    public void run() {
        try {
            while (this.running) {
                switch (this.mode) {
                    case MySQL_Flags.MODE_INIT:
                        this.logger.trace("MODE_INIT");
                        this.nextMode = MySQL_Flags.MODE_READ_HANDSHAKE;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.init(this);
                    
                    case MySQL_Flags.MODE_READ_HANDSHAKE:
                        this.logger.trace("MODE_READ_HANDSHAKE");
                        this.nextMode = MySQL_Flags.MODE_SEND_HANDSHAKE;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.read_handshake(this);
                        break;
                    
                    case MySQL_Flags.MODE_SEND_HANDSHAKE:
                        this.logger.trace("MODE_SEND_HANDSHAKE");
                        this.nextMode = MySQL_Flags.MODE_READ_AUTH;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.send_handshake(this);
                        break;
                    
                    case MySQL_Flags.MODE_READ_AUTH:
                        this.logger.trace("MODE_READ_AUTH");
                        this.nextMode = MySQL_Flags.MODE_SEND_AUTH;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.read_auth(this);
                        break;
                    
                    case MySQL_Flags.MODE_SEND_AUTH:
                        this.logger.trace("MODE_SEND_AUTH");
                        this.nextMode = MySQL_Flags.MODE_READ_AUTH_RESULT;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.send_auth(this);
                        break;
                    
                    case MySQL_Flags.MODE_READ_AUTH_RESULT:
                        this.logger.trace("MODE_READ_AUTH_RESULT");
                        this.nextMode = MySQL_Flags.MODE_SEND_AUTH_RESULT;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.read_auth_result(this);
                        break;
                    
                    case MySQL_Flags.MODE_SEND_AUTH_RESULT:
                        this.logger.trace("MODE_SEND_AUTH_RESULT");
                        this.nextMode = MySQL_Flags.MODE_READ_QUERY;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.send_auth_result(this);
                        break;
                    
                    case MySQL_Flags.MODE_READ_QUERY:
                        this.logger.trace("MODE_READ_QUERY");
                        this.nextMode = MySQL_Flags.MODE_SEND_QUERY;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.read_query(this);
                        break;
                    
                    case MySQL_Flags.MODE_SEND_QUERY:
                        this.logger.trace("MODE_SEND_QUERY");
                        this.nextMode = MySQL_Flags.MODE_READ_QUERY_RESULT;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.send_query(this);
                        break;
                    
                    case MySQL_Flags.MODE_READ_QUERY_RESULT:
                        this.logger.trace("MODE_READ_QUERY_RESULT");
                        this.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.read_query_result(this);
                        break;
                    
                    case MySQL_Flags.MODE_SEND_QUERY_RESULT:
                        this.logger.trace("MODE_SEND_QUERY_RESULT");
                        this.nextMode = MySQL_Flags.MODE_READ_QUERY;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.send_query_result(this);
                        break;
                    
                    case MySQL_Flags.MODE_CLEANUP:
                        this.logger.trace("MODE_CLEANUP");
                        this.nextMode = MySQL_Flags.MODE_CLEANUP;
                        for (Proxy_Plugin plugin : this.plugins)
                            plugin.cleanup(this);
                        this.halt();
                        break;
                    
                    default:
                        this.logger.fatal("UNKNOWN MODE "+this.mode);
                        this.halt();
                        break;
                }
                this.mode = this.nextMode;
            }
            
            try {
                this.clientSocket.close();
            }
            catch (IOException e) {}
            
            this.logger.info("Exiting thread.");
        }
        finally {
            try {
                this.clientSocket.close();
            }
            catch (IOException e) {}
        }
    }
    
    public void buffer_result_set() {
        if (!this.bufferResultSet)
            this.bufferResultSet = true;
    }
    
    public void halt() {
        this.logger.info("Halting!");
        this.mode = MySQL_Flags.MODE_CLEANUP;
        this.nextMode = MySQL_Flags.MODE_CLEANUP;
        this.running = false;
    }
    
    public void clear_buffer() {
        this.logger.trace("Clearing Buffer.");
        this.offset = 0;
        this.packet_id = 0;
        
        // With how ehcache works, if we clear the buffer via .clear(), it also
        // clears the cached value. Create a new ArrayList and count on java
        // cleaning up after ourselves.
        this.buffer = new ArrayList<byte[]>();
    }
    
    public byte[] get_packet(int packet_id) {
        if (packet_id >= this.buffer.size()) {
            this.logger.trace("Packet id "+packet_id+" is null!");
            this.halt();
            return null;
        }
        
        return this.buffer.get(packet_id);
    }
    
    public byte[] get_packet() {
        return this.get_packet(this.packet_id);
    }
    
    public void read_full_result_set(InputStream in) {
        this.logger.trace("read_full_result_set");
        // Assume we have the start of a result set already
        
        byte[] packet = this.buffer.get(this.packet_id);
        long colCount = MySQL_ColCount.loadFromPacket(packet).colCount;
        this.logger.trace("colCount "+colCount);
        
        // Read the columns and the EOF field
        for (int i = 0; i < (colCount+1); i++) {
            
            // Evil optimization
            if (!this.bufferResultSet)
                this.write(this.clientOut);
                
            packet = this.read_packet(in);
            if (packet == null) {
                this.halt();
                return;
            }
        }
        
        do {
            // Evil optimization
            if (!this.bufferResultSet)
                this.write(this.clientOut);
            
            packet = this.read_packet(in);
            if (packet == null) {
                this.halt();
                return;
            }
        } while (MySQL_Packet.getType(packet) != MySQL_Flags.EOF && MySQL_Packet.getType(packet) != MySQL_Flags.ERR);
        
        // Evil optimization
            if (!this.bufferResultSet)
                this.write(this.clientOut);
        
        if (MySQL_Packet.getType(packet) == MySQL_Flags.ERR)
            return;
        
        if (MySQL_EOF.loadFromPacket(packet).hasStatusFlag(MySQL_Flags.SERVER_MORE_RESULTS_EXISTS)) {
            this.logger.trace("More Result Sets.");
            this.read_packet(in);
            this.read_full_result_set(in);
        }
    }
    
    public byte[] read_packet(InputStream in) {
        this.logger.trace("read_packet");
        this.logger.trace(in);
        int b = 0;
        int size = 0;
        byte[] packet = new byte[3];
        
        try {
            // Read size (3)
            int offset = 0;
            int target = 3;
            do {
                b = in.read(packet, offset, (target - offset));
                if (b == -1) {
                    this.halt();
                    return null;
                }
                offset += b;
            } while (offset != target);
        }
        catch (IOException e) {
            this.logger.fatal("IOException: "+e);
            this.halt();
            return null;
        }
        
        size = MySQL_Packet.getSize(packet);
        
        byte[] packet_tmp = new byte[size+4];
        System.arraycopy(packet, 0, packet_tmp, 0, 3);
        packet = packet_tmp;
        packet_tmp = null;
        
        try {
            int offset = 3;
            int target = packet.length;
            do {
                b = in.read(packet, offset, (target - offset));
                if (b == -1) {
                    this.halt();
                    return null;
                }
                offset += b;
            } while (offset != target);
        }
        catch (IOException e) {
            this.logger.fatal("IOException: "+e);
            this.halt();
            return null;
        }
        
        this.packet_id = this.buffer.size();
        MySQL_Packet.dump(packet);
        this.buffer.add(packet);
        return packet;
    }
    
    public void write(OutputStream out) {
        this.logger.trace("write");
        this.logger.trace(out);
        
        for (byte[] packet: this.buffer) {
            this.logger.trace("Writing packet size "+packet.length);
            try {
                MySQL_Packet.dump(packet);
                out.write(packet);
            }
            catch (IOException e) {
                this.halt();
                this.logger.fatal("IOException: "+e);
                return;
            }
        }
        this.clear_buffer();
    }
}
