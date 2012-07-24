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
    
    // Where to connect to
    public String mysqlHost = null;
    public int mysqlPort;
    
    // MySql server stuff
    public Socket mysqlSocket = null;
    public InputStream mysqlIn = null;
    public OutputStream mysqlOut = null;
    
    // Client stuff
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
    public int mode = 0;
    
    // Allow plugins to muck with the modes
    public int nextMode = 0;
    
    public Proxy(Socket clientSocket, String mysqlHost, int mysqlPort, ArrayList<Proxy_Plugin> plugins) {
        this.clientSocket = clientSocket;
        this.mysqlHost = mysqlHost;
        this.mysqlPort = mysqlPort;
        this.plugins = plugins;
        
        try {
            this.clientIn = this.clientSocket.getInputStream();
            this.clientOut = this.clientSocket.getOutputStream();
        
            // Connect to the mysql server on the other side
            this.mysqlSocket = new Socket(this.mysqlHost, this.mysqlPort);
            this.mysqlIn = this.mysqlSocket.getInputStream();
            this.mysqlOut = this.mysqlSocket.getOutputStream();
        }
        catch (IOException e) {
            this.logger.fatal("IOException: "+e);
            this.running = false;
            return;
        }
    }

    public void run() {
        try {
            this.logger.trace("MODE_INIT");
            this.mode = MySQL_Flags.MODE_INIT;
            this.nextMode = MySQL_Flags.MODE_READ_HANDSHAKE;
            this.call_plugins();
            this.mode = this.nextMode;
    
            while (this.running) {
                
                switch (this.mode) {
                    case MySQL_Flags.MODE_READ_HANDSHAKE:
                        this.logger.trace("MODE_READ_HANDSHAKE");
                        this.nextMode = MySQL_Flags.MODE_SEND_HANDSHAKE;
                        this.read_handshake();
                        break;
                    
                    case MySQL_Flags.MODE_SEND_HANDSHAKE:
                        this.logger.trace("MODE_SEND_HANDSHAKE");
                        this.nextMode = MySQL_Flags.MODE_READ_AUTH;
                        this.send_handshake();
                        break;
                    
                    case MySQL_Flags.MODE_READ_AUTH:
                        this.logger.trace("MODE_READ_AUTH");
                        this.nextMode = MySQL_Flags.MODE_SEND_AUTH;
                        this.read_auth();
                        break;
                    
                    case MySQL_Flags.MODE_SEND_AUTH:
                        this.logger.trace("MODE_SEND_AUTH");
                        this.nextMode = MySQL_Flags.MODE_READ_AUTH_RESULT;
                        this.send_auth();
                        break;
                    
                    case MySQL_Flags.MODE_READ_AUTH_RESULT:
                        this.logger.trace("MODE_READ_AUTH_RESULT");
                        this.nextMode = MySQL_Flags.MODE_SEND_AUTH_RESULT;
                        this.read_auth_result();
                        break;
                    
                    case MySQL_Flags.MODE_SEND_AUTH_RESULT:
                        this.logger.trace("MODE_SEND_AUTH_RESULT");
                        this.nextMode = MySQL_Flags.MODE_READ_QUERY;
                        this.send_auth_result();
                        break;
                    
                    case MySQL_Flags.MODE_READ_QUERY:
                        this.logger.trace("MODE_READ_QUERY");
                        this.nextMode = MySQL_Flags.MODE_SEND_QUERY;
                        this.read_query();
                        break;
                    
                    case MySQL_Flags.MODE_SEND_QUERY:
                        this.logger.trace("MODE_SEND_QUERY");
                        this.nextMode = MySQL_Flags.MODE_READ_QUERY_RESULT;
                        this.send_query();
                        break;
                    
                    case MySQL_Flags.MODE_READ_QUERY_RESULT:
                        this.logger.trace("MODE_READ_QUERY_RESULT");
                        this.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
                        this.read_query_result();
                        break;
                    
                    case MySQL_Flags.MODE_SEND_QUERY_RESULT:
                        this.logger.trace("MODE_SEND_QUERY_RESULT");
                        this.nextMode = MySQL_Flags.MODE_READ_QUERY;
                        this.send_query_result();
                        break;
                    
                    default:
                        this.logger.fatal("UNKNOWN MODE "+this.mode);
                        this.halt();
                        break;
                }
                this.call_plugins();
                this.mode = this.nextMode;
            }
            
            this.mode = MySQL_Flags.MODE_CLEANUP;
            this.nextMode = MySQL_Flags.MODE_CLEANUP;
            this.logger.trace("MODE_CLEANUP");
            this.call_plugins();
            
            try {
                this.mysqlSocket.close();
            }
            catch (IOException e) {}
            
            try {
                this.clientSocket.close();
            }
            catch (IOException e) {}
            
            this.logger.info("Exiting thread.");
        }
        finally {
            try {
                this.mysqlSocket.close();
            }
            catch (IOException e) {}
            
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
    
    public void call_plugins() {
        for (Proxy_Plugin plugin : this.plugins) {
            switch (this.mode) {
                case MySQL_Flags.MODE_INIT:
                    plugin.init(this);
                    break;
                
                case MySQL_Flags.MODE_READ_HANDSHAKE:
                    plugin.read_handshake(this);
                    break;
                
                case MySQL_Flags.MODE_SEND_HANDSHAKE:
                    plugin.send_handshake(this);
                    break;
                
                case MySQL_Flags.MODE_READ_AUTH:
                    plugin.read_auth(this);
                    break;
                
                case MySQL_Flags.MODE_SEND_AUTH:
                    plugin.send_auth(this);
                    break;
                
                case MySQL_Flags.MODE_READ_AUTH_RESULT:
                    plugin.read_auth_result(this);
                    break;
                
                case MySQL_Flags.MODE_SEND_AUTH_RESULT:
                    plugin.send_auth_result(this);
                    break;
                
                case MySQL_Flags.MODE_READ_QUERY:
                    plugin.read_query(this);
                    break;
                
                case MySQL_Flags.MODE_SEND_QUERY:
                    plugin.send_query(this);
                    break;
                
                case MySQL_Flags.MODE_READ_QUERY_RESULT:
                    plugin.read_query_result(this);
                    break;
                
                case MySQL_Flags.MODE_SEND_QUERY_RESULT:
                    plugin.send_query_result(this);
                    break;
                
                case MySQL_Flags.MODE_CLEANUP:
                    plugin.cleanup(this);
                    break;
                
                default:
                    this.logger.fatal("UNKNOWN MODE "+this.mode);
                    this.halt();
                    break;
            }
        }
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
        
        // Read the columns and the EOF field
        for (int i = 0; i < (colCount+1); i++) {
            if (!this.bufferResultSet)
                this.send_query_result();
            packet = this.read_packet(this.mysqlIn);
            if (packet == null) {
                this.halt();
                return;
            }
        }
        
        do {
            if (!this.bufferResultSet)
                this.send_query_result();
            packet = this.read_packet(this.mysqlIn);
            if (packet == null) {
                this.halt();
                return;
            }
        } while (MySQL_Packet.getType(packet) != MySQL_Flags.EOF && MySQL_Packet.getType(packet) != MySQL_Flags.ERR);
        
        if (!this.bufferResultSet)
                this.send_query_result();
        
        if (MySQL_Packet.getType(packet) == MySQL_Flags.ERR)
            return;
        
        if (MySQL_EOF.loadFromPacket(packet).hasStatusFlag(MySQL_Flags.SERVER_MORE_RESULTS_EXISTS)) {
            this.logger.trace("More Result Sets.");
            this.read_packet(this.mysqlIn);
            this.read_full_result_set(this.mysqlIn);
        }
    }
    
    public byte[] read_packet(InputStream in) {
        this.logger.trace("read_packet");
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
        
        size = (int)MySQL_Proto.get_fixed_int(packet);
        
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
        this.buffer.add(packet);
        return packet;
    }
    
    public void write(OutputStream out) {
        this.logger.trace("write");
        
        for (byte[] packet: this.buffer) {
            this.logger.trace("Writing packet size "+packet.length);
            try {
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
    
    public void read_handshake() {
        this.logger.trace("read_handshake");
        this.read_packet(this.mysqlIn);
        
        this.authChallenge = MySQL_Auth_Challenge.loadFromPacket(this.buffer.get(this.packet_id));
        
        // Remove some flags from the reply
        this.authChallenge.removeCapabilityFlag(MySQL_Flags.CLIENT_COMPRESS);
        this.authChallenge.removeCapabilityFlag(MySQL_Flags.CLIENT_SSL);
        
        // Set the default result set creation to the server's character set
        MySQL_ResultSet_Text.characterSet = authChallenge.characterSet;
        
        // Set Replace the packet in the buffer
        this.buffer.set(this.packet_id, this.authChallenge.toPacket());
    }
    
    public void send_handshake() {
        this.logger.trace("send_handshake");
        this.write(this.clientOut);
    }
    
    public void read_auth() {
        this.logger.trace("read_auth");
        this.read_packet(this.clientIn);
        
        this.authReply = MySQL_Auth_Response.loadFromPacket(this.buffer.get(this.packet_id));
        
        if (!this.authReply.hasCapabilityFlag(MySQL_Flags.CLIENT_PROTOCOL_41)) {
            this.logger.fatal("We do not support Protocols under 4.1");
            this.halt();
            return;
        }
        
        this.authReply.removeCapabilityFlag(MySQL_Flags.CLIENT_COMPRESS);
        this.authReply.removeCapabilityFlag(MySQL_Flags.CLIENT_SSL);
        
        this.schema = this.authReply.schema;
    }
    
    public void send_auth() {
        this.logger.trace("send_auth");
        this.write(this.mysqlOut);
    }
    
    public void read_auth_result() {
        this.logger.trace("read_auth_result");
        byte[] packet = this.read_packet(this.mysqlIn);
        if (MySQL_Packet.getType(packet) != MySQL_Flags.OK) {
            this.logger.fatal("Auth is not okay!");
            this.halt();
        }
    }
    
    public void send_auth_result() {
        this.logger.trace("read_auth_result");
        this.write(this.clientOut);
    }
    
    public void read_query() {
        this.logger.trace("read_query");
        this.bufferResultSet = false;
        
        byte[] packet = this.read_packet(this.clientIn);
        
        this.sequenceId = MySQL_Packet.getSequenceId(packet);
        this.logger.trace("Client sequenceId: "+this.sequenceId);
        
        switch (MySQL_Packet.getType(packet)) {
            case MySQL_Flags.COM_QUIT:
                this.logger.trace("COM_QUIT");
                this.halt();
                break;
            
            // Extract out the new default schema
            case MySQL_Flags.COM_INIT_DB:
                this.logger.trace("COM_INIT_DB");
                this.schema = MySQL_Com_Initdb.loadFromPacket(packet).schema;
                break;
            
            // Query
            case MySQL_Flags.COM_QUERY:
                this.logger.trace("COM_QUERY");
                this.query = MySQL_Com_Query.loadFromPacket(packet).query;
                break;
            
            default:
                break;
        }
    }
    
    public void send_query(){
        this.logger.trace("send_query");
        this.write(this.mysqlOut);
    }
    
    public void read_query_result() {
        this.logger.trace("read_query_result");
        
        byte[] packet = this.read_packet(this.mysqlIn);
        
        this.sequenceId = MySQL_Packet.getSequenceId(packet);
        
        switch (MySQL_Packet.getType(packet)) {
            case MySQL_Flags.OK:
            case MySQL_Flags.ERR:
                break;
            
            default:
                this.read_full_result_set(this.mysqlIn);
                break;
        }
    }
    
    public void send_query_result(){
        this.logger.trace("send_query_result");
        this.write(this.clientOut);
    }
    
}
