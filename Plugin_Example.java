import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.commons.io.*;

public class Plugin_Example implements Proxy_Plugin {
    public void init(Proxy context) {
        System.err.print("Plugin_Example->init\n");
    }
    
    public void read_handshake(Proxy context) {
        System.err.print("Plugin_Example->read_handshake\n");
    }
    
    public void read_auth(Proxy context) {
        System.err.print("Plugin_Example->read_auth\n");
    }
    
    public void read_auth_result(Proxy context) {
        System.err.print("Plugin_Example->read_auth_result\n");
    }
    
    public void read_query(Proxy context) {
        System.err.print("Plugin_Example->read_query\n");
    }
    
    public void read_query_result(Proxy context) {
        System.err.print("Plugin_Example->read_query_result\n");
    }
    
    public void send_query_result(Proxy context) {
        System.err.print("Plugin_Example->send_query_result\n");
    }
    
    public void cleanup(Proxy context) {
        System.err.print("Plugin_Example->cleanup\n");
    }
    
}
