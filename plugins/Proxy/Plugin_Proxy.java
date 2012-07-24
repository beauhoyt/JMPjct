import java.net.*;
import java.io.*;
import org.apache.log4j.Logger;

public class Plugin_Proxy extends Plugin_Base {
    public Logger logger = Logger.getLogger("Plugin.Proxy");
    
    // MySql server stuff
    public String mysqlHost = "127.0.0.1";
    public int mysqlPort = 3306;
    public Socket mysqlSocket = null;
    public InputStream mysqlIn = null;
    public OutputStream mysqlOut = null;
    
    public Plugin_Proxy() {
        this.logger.trace("Plugin_Proxy");
        // TODO Parse system props to figure out which server we're proxying to
        this.mysqlHost = "127.0.0.1";
        this.mysqlPort = 3306;
    }
    
    public void init(Proxy context) {
        this.logger.trace("init");
        try {
            // Connect to the mysql server on the other side
            this.mysqlSocket = new Socket(this.mysqlHost, this.mysqlPort);
            this.logger.info("Connected to mysql server at "+this.mysqlHost+":"+this.mysqlPort);
            this.mysqlIn = this.mysqlSocket.getInputStream();
            this.mysqlOut = this.mysqlSocket.getOutputStream();
        }
        catch (java.net.UnknownHostException e){
            this.logger.fatal(e);
            context.halt();
        }
        catch (java.io.IOException e){
            this.logger.fatal(e);
            context.halt();
        }
    }
    
    public void read_handshake(Proxy context) {
        this.logger.trace("read_handshake");
        byte[] packet = context.read_packet(this.mysqlIn);
        
        context.authChallenge = MySQL_Auth_Challenge.loadFromPacket(packet);
        
        // Remove some flags from the reply
        context.authChallenge.removeCapabilityFlag(MySQL_Flags.CLIENT_COMPRESS);
        context.authChallenge.removeCapabilityFlag(MySQL_Flags.CLIENT_SSL);
        
        // Set the default result set creation to the server's character set
        MySQL_ResultSet_Text.characterSet = context.authChallenge.characterSet;
        
        // Set Replace the packet in the buffer
        context.buffer.set(context.packet_id, context.authChallenge.toPacket());
    }
    
    public void send_handshake(Proxy context) {
        this.logger.trace("send_handshake");
        context.write(context.clientOut);
    }
    
    public void read_auth(Proxy context) {
        this.logger.trace("read_auth");
        byte[] packet = context.read_packet(context.clientIn);
        
        context.authReply = MySQL_Auth_Response.loadFromPacket(packet);
        
        if (!context.authReply.hasCapabilityFlag(MySQL_Flags.CLIENT_PROTOCOL_41)) {
            this.logger.fatal("We do not support Protocols under 4.1");
            context.halt();
            return;
        }
        
        context.authReply.removeCapabilityFlag(MySQL_Flags.CLIENT_COMPRESS);
        context.authReply.removeCapabilityFlag(MySQL_Flags.CLIENT_SSL);
        
        context.schema = context.authReply.schema;
    }
    
    public void send_auth(Proxy Proxy) {
        this.logger.trace("send_auth");
        Proxy.write(this.mysqlOut);
    }
    
    public void read_auth_result(Proxy context) {
        this.logger.trace("read_auth_result");
        byte[] packet = context.read_packet(this.mysqlIn);
        if (MySQL_Packet.getType(packet) != MySQL_Flags.OK) {
            this.logger.fatal("Auth is not okay!");
            context.halt();
        }
    }
    
    public void send_auth_result(Proxy context) {
        this.logger.trace("read_auth_result");
        context.write(context.clientOut);
    }
    
    public void read_query(Proxy context) {
        this.logger.trace("read_query");
        context.bufferResultSet = false;
        
        byte[] packet = context.read_packet(context.clientIn);
        
        context.sequenceId = MySQL_Packet.getSequenceId(packet);
        this.logger.trace("Client sequenceId: "+context.sequenceId);
        
        switch (MySQL_Packet.getType(packet)) {
            case MySQL_Flags.COM_QUIT:
                this.logger.trace("COM_QUIT");
                context.halt();
                break;
            
            // Extract out the new default schema
            case MySQL_Flags.COM_INIT_DB:
                this.logger.trace("COM_INIT_DB");
                context.schema = MySQL_Com_Initdb.loadFromPacket(packet).schema;
                break;
            
            // Query
            case MySQL_Flags.COM_QUERY:
                this.logger.trace("COM_QUERY");
                context.query = MySQL_Com_Query.loadFromPacket(packet).query;
                break;
            
            default:
                break;
        }
    }
    
    public void send_query(Proxy context) {
        this.logger.trace("send_query");
        context.write(this.mysqlOut);
    }
    
    public void read_query_result(Proxy context) {
        this.logger.trace("read_query_result");
        
        byte[] packet = context.read_packet(this.mysqlIn);
        
        context.sequenceId = MySQL_Packet.getSequenceId(packet);
        
        switch (MySQL_Packet.getType(packet)) {
            case MySQL_Flags.OK:
            case MySQL_Flags.ERR:
                break;
            
            default:
                context.read_full_result_set(this.mysqlIn);
                break;
        }
    }
    
    public void send_query_result(Proxy context) {
        this.logger.trace("send_query_result");
        context.write(context.clientOut);
    }
    
    public void cleanup(Proxy context) {
        this.logger.trace("cleanup");
        try {
            this.mysqlSocket.close();
        }
        catch (java.io.IOException e){
            this.logger.fatal(e);
            context.halt();
        }
    }
}
