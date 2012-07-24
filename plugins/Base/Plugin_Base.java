/*
 * Create an empty abstract class to allow plugins to only
 * implement their required differences.
 */
import org.apache.log4j.Logger;

public abstract class Plugin_Base implements Proxy_Plugin {
    public Logger logger = Logger.getLogger("Plugin.Base");
    
    public void init(Proxy context) {}
    
    public void read_handshake(Proxy context) {}
    public void send_handshake(Proxy context) {}
    
    public void read_auth(Proxy context) {}
    public void send_auth(Proxy context) {}
    
    public void read_auth_result(Proxy context) {}
    public void send_auth_result(Proxy context) {}
    
    public void read_query(Proxy context) {}
    public void send_query(Proxy context) {}
    
    public void read_query_result(Proxy context) {}
    public void send_query_result(Proxy context) {}
    
    public void cleanup(Proxy context) {}
}
