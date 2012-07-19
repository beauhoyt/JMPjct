import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.commons.io.*;

public class Plugin_Debug implements Proxy_Plugin {
    public void init(Proxy context) {
        System.err.print("Plugin_Debug->init\n");
        System.err.print("Connected to mysql server at "+context.mysqlHost+":"+context.mysqlPort+"\n\n");
        return;
    }
    
    public void read_handshake(Proxy context) {
        System.err.print("Plugin_Debug->read_handshake\n");
        System.err.print("<- AuthChallengePacket\n");
        System.err.print("   Server Version: "+context.serverVersion+"\n");
        System.err.print("   Connection Id: "+context.connectionId+"\n");
        
        System.err.print("   Server Capability Flags: ");
        this.dump_capability_flags(context, 1);
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
        this.dump_capability_flags(context, 0);
        System.err.print("\n\n");
        return;
    }
    
    public void read_auth_result(Proxy context) {
        System.err.print("Plugin_Debug->read_auth_result\n");
        return;
    }
    
    public void read_query(Proxy context) {
        System.err.print("Plugin_Debug->read_query\n");
        switch (context.packetType) {
            case Proxy.COM_QUIT:
                System.err.print("-> COM_QUIT\n");
                break;
            
            // Extract out the new default schema
            case Proxy.COM_INIT_DB:
                System.err.print("-> USE "+context.schema+"\n");
                break;
            
            // Query
            case Proxy.COM_QUERY:
                System.err.print("-> "+context.query+"\n");
                break;
            
            default:
                System.err.print("Packet is "+context.packetType+" type.\n");
                this.dump_buffer(context);
                break;
        }
        return;
    }
    
    public void read_query_result(Proxy context) {
        System.err.print("Plugin_Debug->read_query_result\n");
        switch (context.packetType) {
            case Proxy.OK:
                System.err.print("<- OK\n");
                if (context.affectedRows > 0)
                    System.err.print("   Affected rows: "+context.affectedRows+"\n");
                if (context.lastInsertId > 0)
                    System.err.print("   Inserted id: "+context.lastInsertId+"\n");
                if (context.warnings > 0)
                    System.err.print("   Warnings: "+context.warnings+"\n");

                System.err.print("   Status Flags: ");
                this.dump_status_flags(context);
                System.err.print("\n");
                break;
            
            case Proxy.ERR:
                System.err.print("<- ERR\n");
                break;
            
            default:
                System.err.print("Result or Packet is "+context.packetType+" type.\n");
                break;
        }
        return;
    }
    
    public void send_query_result(Proxy context) {
        System.err.print("Plugin_Debug->send_query_result\n");
        return;
    }
    
    public void cleanup(Proxy context) {
        System.err.print("Plugin_Debug->cleanup\n");
        return;
    }
    
    public void dump_buffer(Proxy context) {
        int size = context.buffer.size();
        Integer b = 0;
        int i = 0;
        byte[] buff = new byte[size];
        
        for (i = 0; i < size; i++) {
            b = context.buffer.get(i);
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
    
    public void dump_capability_flags(Proxy context, Integer server) {
        Integer capabilityFlags = 0;
        if (server == 0)
            capabilityFlags = context.clientCapabilityFlags;
        else
            capabilityFlags = context.serverCapabilityFlags;
            
        if (capabilityFlags > 0) {
            if ((capabilityFlags & Proxy.CLIENT_LONG_PASSWORD) != 0)
                System.err.print(" CLIENT_LONG_PASSWORD");
            if ((capabilityFlags & Proxy.CLIENT_FOUND_ROWS) != 0)
                System.err.print(" CLIENT_FOUND_ROWS");
            if ((capabilityFlags & Proxy.CLIENT_LONG_FLAG) != 0)
                System.err.print(" CLIENT_LONG_FLAG");
            if ((capabilityFlags & Proxy.CLIENT_CONNECT_WITH_DB) != 0)
                System.err.print(" CLIENT_CONNECT_WITH_DB");
            if ((capabilityFlags & Proxy.CLIENT_NO_SCHEMA) != 0)
                System.err.print(" CLIENT_NO_SCHEMA");
            if ((capabilityFlags & Proxy.CLIENT_COMPRESS) != 0)
                System.err.print(" CLIENT_COMPRESS");
            if ((capabilityFlags & Proxy.CLIENT_ODBC) != 0)
                System.err.print(" CLIENT_ODBC");
            if ((capabilityFlags & Proxy.CLIENT_LOCAL_FILES) != 0)
                System.err.print(" CLIENT_LOCAL_FILES");
            if ((capabilityFlags & Proxy.CLIENT_IGNORE_SPACE) != 0)
                System.err.print(" CLIENT_IGNORE_SPACE");
            if ((capabilityFlags & Proxy.CLIENT_PROTOCOL_41) != 0)
                System.err.print(" CLIENT_PROTOCOL_41");
            if ((capabilityFlags & Proxy.CLIENT_INTERACTIVE) != 0)
                System.err.print(" CLIENT_INTERACTIVE");
            if ((capabilityFlags & Proxy.CLIENT_SSL) != 0)
                System.err.print(" CLIENT_SSL");
            if ((capabilityFlags & Proxy.CLIENT_IGNORE_SIGPIPE) != 0)
                System.err.print(" CLIENT_IGNORE_SIGPIPE");
            if ((capabilityFlags & Proxy.CLIENT_TRANSACTIONS) != 0)
                System.err.print(" CLIENT_TRANSACTIONS");
            if ((capabilityFlags & Proxy.CLIENT_RESERVED) != 0)
                System.err.print(" CLIENT_RESERVED");
            if ((capabilityFlags & Proxy.CLIENT_SECURE_CONNECTION) != 0)
                System.err.print(" CLIENT_SECURE_CONNECTION");
        }
    }
    
    public void dump_status_flags(Proxy context) {
        if (context.statusFlags > 0) {
            if ((context.statusFlags & Proxy.SERVER_STATUS_IN_TRANS) != 0)
                System.err.print(" SERVER_STATUS_IN_TRANS");
            if ((context.statusFlags & Proxy.SERVER_STATUS_AUTOCOMMIT) != 0)
                System.err.print(" SERVER_STATUS_AUTOCOMMIT");
            if ((context.statusFlags & Proxy.SERVER_MORE_RESULTS_EXISTS) != 0)
                System.err.print(" SERVER_MORE_RESULTS_EXISTS");
            if ((context.statusFlags & Proxy.SERVER_STATUS_NO_GOOD_INDEX_USED) != 0)
                System.err.print(" SERVER_STATUS_NO_GOOD_INDEX_USED");
            if ((context.statusFlags & Proxy.SERVER_STATUS_NO_INDEX_USED) != 0)
                System.err.print(" SERVER_STATUS_NO_INDEX_USED");
            if ((context.statusFlags & Proxy.SERVER_STATUS_CURSOR_EXISTS) != 0)
                System.err.print(" SERVER_STATUS_CURSOR_EXISTS");
            if ((context.statusFlags & Proxy.SERVER_STATUS_LAST_ROW_SENT) != 0)
                System.err.print(" SERVER_STATUS_LAST_ROW_SENT");
            if ((context.statusFlags & Proxy.SERVER_STATUS_LAST_ROW_SENT) != 0)
                System.err.print(" SERVER_STATUS_LAST_ROW_SENT");
            if ((context.statusFlags & Proxy.SERVER_STATUS_DB_DROPPED) != 0)
                System.err.print(" SERVER_STATUS_DB_DROPPED");
            if ((context.statusFlags & Proxy.SERVER_STATUS_NO_BACKSLASH_ESCAPES) != 0)
                System.err.print(" SERVER_STATUS_NO_BACKSLASH_ESCAPES");
            if ((context.statusFlags & Proxy.SERVER_STATUS_METADATA_CHANGED) != 0)
                System.err.print(" SERVER_STATUS_METADATA_CHANGED");
            if ((context.statusFlags & Proxy.SERVER_QUERY_WAS_SLOW) != 0)
                System.err.print(" SERVER_QUERY_WAS_SLOW");
            if ((context.statusFlags & Proxy.SERVER_PS_OUT_PARAMS) != 0)
                System.err.print(" SERVER_PS_OUT_PARAMS");
        }
    }
}
