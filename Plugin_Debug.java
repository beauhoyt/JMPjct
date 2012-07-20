/*
 * Debug plugin
 * Output packet debugging information
 */

import java.io.*;
import org.apache.commons.io.*;

public class Plugin_Debug extends Plugin_Base {
    public void init(Proxy context) {
        //System.err.print("\nPlugin_Debug->init\n");
        System.err.print("Connected to mysql server at "+context.mysqlHost+":"+context.mysqlPort+"\n\n");
        return;
    }
    
    public void read_handshake(Proxy context) {
        System.err.print("Plugin_Debug->read_handshake\n");
        System.err.print("<- AuthChallengePacket\n");
        System.err.print("   Server Version: "+context.serverVersion+"\n");
        System.err.print("   Connection Id: "+context.connectionId+"\n");
        
        System.err.print("   Server Capability Flags: ");
        Plugin_Debug.dump_capability_flags(context.serverCapabilityFlags);
        System.err.print("\n\n");
        return;
    }
    
    public void read_auth(Proxy context) {
        System.err.print("Plugin_Debug->read_auth\n");
        System.err.print("-> AuthResponsePacket\n");
        System.err.print("   Max Packet Size: "+context.clientMaxPacketSize+"\n");
        System.err.print("   User: "+context.user+"\n");
        System.err.print("   Schema: "+context.schema+"\n");
        
        System.err.print("   Client Capability Flags: ");
        Plugin_Debug.dump_capability_flags(context.clientCapabilityFlags);
        System.err.print("\n\n");
        return;
    }
    
    public void read_auth_result(Proxy context) {
        //System.err.print("\nPlugin_Debug->read_auth_result\n");
        return;
    }
    
    public void read_query(Proxy context) {
        //System.err.print("\nPlugin_Debug->read_query\n");
        switch (context.packetType) {
            case MySQL_Flags.COM_QUIT:
                System.err.print("\n-> COM_QUIT\n");
                break;
            
            // Extract out the new default schema
            case MySQL_Flags.COM_INIT_DB:
                System.err.print("\n-> USE "+context.schema+"\n");
                break;
            
            // Query
            case MySQL_Flags.COM_QUERY:
                System.err.print("\n-> "+context.query+"\n");
                break;
            
            default:
                System.err.print("Packet is "+context.packetType+" type.\n");
                Plugin_Debug.dump_buffer(context);
                break;
        }
        return;
    }
    
    public void read_query_result(Proxy context) {
        //System.err.print("\nPlugin_Debug->read_query_result\n");
        switch (context.packetType) {
            case MySQL_Flags.OK:
                System.err.print("<- OK\n");
                if (context.affectedRows > 0)
                    System.err.print("   Affected rows: "+context.affectedRows+"\n");
                if (context.lastInsertId > 0)
                    System.err.print("   Inserted id: "+context.lastInsertId+"\n");
                if (context.warnings > 0)
                    System.err.print("   Warnings: "+context.warnings+"\n");

                System.err.print("   Status Flags: ");
                Plugin_Debug.dump_status_flags(context.statusFlags);
                System.err.print("\n");
                break;
            
            case MySQL_Flags.ERR:
                System.err.print("<- ERR\n");
                break;
            
            default:
                System.err.print("Result set or Packet is "+context.packetType+" type.\n");
                break;
        }
        return;
    }
    
    public void send_query_result(Proxy context) {
        //System.err.print("\nPlugin_Debug->send_query_result\n");
        return;
    }
    
    public void cleanup(Proxy context) {
        //System.err.print("\nPlugin_Debug->cleanup\n");
        return;
    }
    
    public static final void dump_buffer(Proxy context) {
        for (int i = 0; i < context.buffer.size(); i++) {
            Plugin_Debug.dump_packet(context.buffer.get(i));
        }
    }
    
    public static final void dump_packet(byte[] packet) {
        try {
                HexDump.dump(packet, 0, java.lang.System.err, 0);
            }
            catch (IOException e) {
                return;
            }
    }
    
    public static final void dump_capability_flags(long capabilityFlags) {
        if ((capabilityFlags & MySQL_Flags.CLIENT_LONG_PASSWORD) != 0)
            System.err.print(" CLIENT_LONG_PASSWORD");
        if ((capabilityFlags & MySQL_Flags.CLIENT_FOUND_ROWS) != 0)
            System.err.print(" CLIENT_FOUND_ROWS");
        if ((capabilityFlags & MySQL_Flags.CLIENT_LONG_FLAG) != 0)
            System.err.print(" CLIENT_LONG_FLAG");
        if ((capabilityFlags & MySQL_Flags.CLIENT_CONNECT_WITH_DB) != 0)
            System.err.print(" CLIENT_CONNECT_WITH_DB");
        if ((capabilityFlags & MySQL_Flags.CLIENT_NO_SCHEMA) != 0)
            System.err.print(" CLIENT_NO_SCHEMA");
        if ((capabilityFlags & MySQL_Flags.CLIENT_COMPRESS) != 0)
            System.err.print(" CLIENT_COMPRESS");
        if ((capabilityFlags & MySQL_Flags.CLIENT_ODBC) != 0)
            System.err.print(" CLIENT_ODBC");
        if ((capabilityFlags & MySQL_Flags.CLIENT_LOCAL_FILES) != 0)
            System.err.print(" CLIENT_LOCAL_FILES");
        if ((capabilityFlags & MySQL_Flags.CLIENT_IGNORE_SPACE) != 0)
            System.err.print(" CLIENT_IGNORE_SPACE");
        if ((capabilityFlags & MySQL_Flags.CLIENT_PROTOCOL_41) != 0)
            System.err.print(" CLIENT_PROTOCOL_41");
        if ((capabilityFlags & MySQL_Flags.CLIENT_INTERACTIVE) != 0)
            System.err.print(" CLIENT_INTERACTIVE");
        if ((capabilityFlags & MySQL_Flags.CLIENT_SSL) != 0)
            System.err.print(" CLIENT_SSL");
        if ((capabilityFlags & MySQL_Flags.CLIENT_IGNORE_SIGPIPE) != 0)
            System.err.print(" CLIENT_IGNORE_SIGPIPE");
        if ((capabilityFlags & MySQL_Flags.CLIENT_TRANSACTIONS) != 0)
            System.err.print(" CLIENT_TRANSACTIONS");
        if ((capabilityFlags & MySQL_Flags.CLIENT_RESERVED) != 0)
            System.err.print(" CLIENT_RESERVED");
        if ((capabilityFlags & MySQL_Flags.CLIENT_SECURE_CONNECTION) != 0)
            System.err.print(" CLIENT_SECURE_CONNECTION");
    }
    
    public static final void dump_status_flags(long statusFlags) {
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_IN_TRANS) != 0)
            System.err.print(" SERVER_STATUS_IN_TRANS");
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_AUTOCOMMIT) != 0)
            System.err.print(" SERVER_STATUS_AUTOCOMMIT");
        if ((statusFlags & MySQL_Flags.SERVER_MORE_RESULTS_EXISTS) != 0)
            System.err.print(" SERVER_MORE_RESULTS_EXISTS");
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_NO_GOOD_INDEX_USED) != 0)
            System.err.print(" SERVER_STATUS_NO_GOOD_INDEX_USED");
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_NO_INDEX_USED) != 0)
            System.err.print(" SERVER_STATUS_NO_INDEX_USED");
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_CURSOR_EXISTS) != 0)
            System.err.print(" SERVER_STATUS_CURSOR_EXISTS");
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_LAST_ROW_SENT) != 0)
            System.err.print(" SERVER_STATUS_LAST_ROW_SENT");
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_LAST_ROW_SENT) != 0)
            System.err.print(" SERVER_STATUS_LAST_ROW_SENT");
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_DB_DROPPED) != 0)
            System.err.print(" SERVER_STATUS_DB_DROPPED");
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_NO_BACKSLASH_ESCAPES) != 0)
            System.err.print(" SERVER_STATUS_NO_BACKSLASH_ESCAPES");
        if ((statusFlags & MySQL_Flags.SERVER_STATUS_METADATA_CHANGED) != 0)
            System.err.print(" SERVER_STATUS_METADATA_CHANGED");
        if ((statusFlags & MySQL_Flags.SERVER_QUERY_WAS_SLOW) != 0)
            System.err.print(" SERVER_QUERY_WAS_SLOW");
        if ((statusFlags & MySQL_Flags.SERVER_PS_OUT_PARAMS) != 0)
            System.err.print(" SERVER_PS_OUT_PARAMS");
    }
}
