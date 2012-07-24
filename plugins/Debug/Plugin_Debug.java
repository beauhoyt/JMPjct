/*
 * Debug plugin
 * Output packet debugging information
 */

import java.util.*;
import org.apache.log4j.Logger;

public class Plugin_Debug extends Plugin_Base {
    public Logger logger = Logger.getLogger("Plugin.Debug");
    
    public void read_handshake(Engine context) {
        this.logger.debug("<- AuthChallengePacket");
        this.logger.debug("   Server Version: "+context.authChallenge.serverVersion);
        this.logger.debug("   Connection Id: "+context.authChallenge.connectionId);
        this.logger.debug("   Server Capability Flags: "
                          + Plugin_Debug.dump_capability_flags(context.authChallenge.capabilityFlags));
    }
    
    public void read_auth(Engine context) {
        this.logger.debug("-> AuthResponsePacket");
        this.logger.debug("   Max Packet Size: "+context.authReply.maxPacketSize);
        this.logger.debug("   User: "+context.authReply.username);
        this.logger.debug("   Schema: "+context.authReply.schema);
        
        this.logger.debug("   Client Capability Flags: "
                          + Plugin_Debug.dump_capability_flags(context.authReply.capabilityFlags));
    }
    
    public void read_query(Engine context) {
        switch (MySQL_Packet.getType(context.buffer.get(context.buffer.size()-1))) {
            case MySQL_Flags.COM_QUIT:
                this.logger.info("-> COM_QUIT");
                break;
            
            // Extract out the new default schema
            case MySQL_Flags.COM_INIT_DB:
                this.logger.info("-> USE "+context.schema);
                break;
            
            // Query
            case MySQL_Flags.COM_QUERY:
                this.logger.info("-> "+context.query);
                break;
            
            default:
                this.logger.debug("Packet is "+MySQL_Packet.getType(context.buffer.get(context.buffer.size()-1))+" type.");
                Plugin_Debug.dump_buffer(context);
                break;
        }
        context.buffer_result_set();
    }
    
    public void read_query_result(Engine context) {
        if (!context.bufferResultSet)
            return;
        
        switch (MySQL_Packet.getType(context.buffer.get(context.buffer.size()-1))) {
            case MySQL_Flags.OK:
                this.logger.info("<- OK");
                break;
            
            case MySQL_Flags.ERR:
                this.logger.info("<- ERR");
                break;
            
            default:
                this.logger.debug("Result set or Packet is "+MySQL_Packet.getType(context.buffer.get(context.buffer.size()-1))+" type.");
                break;
        }
    }
    
    public static final void dump_buffer(ArrayList<byte[]> buffer) {
        Logger logger = Logger.getLogger("Plugin.Debug");
        
        if (!logger.isTraceEnabled())
            return;
        
        for (byte[] packet: buffer) {
            MySQL_Packet.dump(packet);
        }
    }
    
    public static final void dump_buffer(Engine context) {
        Logger logger = Logger.getLogger("Plugin.Debug");
        
        if (!logger.isTraceEnabled())
            return;
        
        for (byte[] packet: context.buffer) {
            MySQL_Packet.dump(packet);
        }
    }
    
    public static final String dump_capability_flags(long capabilityFlags) {
        String out = "";
        if ((capabilityFlags & MySQL_Flags.CLIENT_LONG_PASSWORD) != 0)
            out += " CLIENT_LONG_PASSWORD";
        if ((capabilityFlags & MySQL_Flags.CLIENT_FOUND_ROWS) != 0)
            out += " CLIENT_FOUND_ROWS";
        if ((capabilityFlags & MySQL_Flags.CLIENT_LONG_FLAG) != 0)
            out += " CLIENT_LONG_FLAG";
        if ((capabilityFlags & MySQL_Flags.CLIENT_CONNECT_WITH_DB) != 0)
            out += " CLIENT_CONNECT_WITH_DB";
        if ((capabilityFlags & MySQL_Flags.CLIENT_NO_SCHEMA) != 0)
            out += " CLIENT_NO_SCHEMA";
        if ((capabilityFlags & MySQL_Flags.CLIENT_COMPRESS) != 0)
            out += " CLIENT_COMPRESS";
        if ((capabilityFlags & MySQL_Flags.CLIENT_ODBC) != 0)
            out += " CLIENT_ODBC";
        if ((capabilityFlags & MySQL_Flags.CLIENT_LOCAL_FILES) != 0)
            out += " CLIENT_LOCAL_FILES";
        if ((capabilityFlags & MySQL_Flags.CLIENT_IGNORE_SPACE) != 0)
            out += " CLIENT_IGNORE_SPACE";
        if ((capabilityFlags & MySQL_Flags.CLIENT_PROTOCOL_41) != 0)
            out += " CLIENT_PROTOCOL_41";
        if ((capabilityFlags & MySQL_Flags.CLIENT_INTERACTIVE) != 0)
            out += " CLIENT_INTERACTIVE";
        if ((capabilityFlags & MySQL_Flags.CLIENT_SSL) != 0)
            out += " CLIENT_SSL";
        if ((capabilityFlags & MySQL_Flags.CLIENT_IGNORE_SIGPIPE) != 0)
            out += " CLIENT_IGNORE_SIGPIPE";
        if ((capabilityFlags & MySQL_Flags.CLIENT_TRANSACTIONS) != 0)
            out += " CLIENT_TRANSACTIONS";
        if ((capabilityFlags & MySQL_Flags.CLIENT_RESERVED) != 0)
            out += " CLIENT_RESERVED";
        if ((capabilityFlags & MySQL_Flags.CLIENT_SECURE_CONNECTION) != 0)
            out += " CLIENT_SECURE_CONNECTION";
        return out;
    }
    
    public static final String dump_status_flags(long statusFlags) {
        String out = "";
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_IN_TRANS) != 0)
            out += " SERVER_STATUS_IN_TRANS";
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_AUTOCOMMIT) != 0)
            out += " SERVER_STATUS_AUTOCOMMIT";
        if ((statusFlags & MySQL_Flags.SERVER_MORE_RESULTS_EXISTS) != 0)
            out += " SERVER_MORE_RESULTS_EXISTS";
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_NO_GOOD_INDEX_USED) != 0)
            out += " SERVER_STATUS_NO_GOOD_INDEX_USED";
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_NO_INDEX_USED) != 0)
            out += " SERVER_STATUS_NO_INDEX_USED";
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_CURSOR_EXISTS) != 0)
            out += " SERVER_STATUS_CURSOR_EXISTS";
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_LAST_ROW_SENT) != 0)
            out += " SERVER_STATUS_LAST_ROW_SENT";
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_LAST_ROW_SENT) != 0)
            out += " SERVER_STATUS_LAST_ROW_SENT";
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_DB_DROPPED) != 0)
            out += " SERVER_STATUS_DB_DROPPED";
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_NO_BACKSLASH_ESCAPES) != 0)
            out += " SERVER_STATUS_NO_BACKSLASH_ESCAPES";
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_METADATA_CHANGED) != 0)
            out += " SERVER_STATUS_METADATA_CHANGED";
        if ((statusFlags & MySQL_Flags.SERVER_QUERY_WAS_SLOW) != 0)
            out += " SERVER_QUERY_WAS_SLOW";
        if ((statusFlags & MySQL_Flags.SERVER_PS_OUT_PARAMS) != 0)
            out += " SERVER_PS_OUT_PARAMS";
        return out;
    }
}
