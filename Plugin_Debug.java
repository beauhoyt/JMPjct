import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.commons.io.*;

public class Plugin_Debug implements Proxy_Plugin {
    public void init(Proxy context){
        System.err.print("Plugin_Debug->init\n");
        return;
    }
    
    public void read_handshake(Proxy context){
        System.err.print("Plugin_Debug->read_handshake\n");
        return;
    }
    
    public void read_auth(Proxy context){
        System.err.print("Plugin_Debug->read_auth\n");
        return;
    }
    
    public void read_auth_result(Proxy context){
        System.err.print("Plugin_Debug->read_auth_result\n");
        return;
    }
    
    public void read_query(Proxy context){
        System.err.print("Plugin_Debug->read_query\n");
        return;
    }
    
    public void read_query_result(Proxy context){
        System.err.print("Plugin_Debug->read_query_result\n");
        return;
    }
    
    public void send_query_result(Proxy context){
        System.err.print("Plugin_Debug->send_query_result\n");
        return;
    }
    
    public void cleanup(Proxy context){
        System.err.print("Plugin_Debug->cleanup\n");
        return;
    }
    
}
