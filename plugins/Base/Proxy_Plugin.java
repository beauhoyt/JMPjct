/*
 * Basic plugin interface.
 */
import org.apache.log4j.Logger;
import java.io.*;

public interface Proxy_Plugin {
    public Logger logger =  Logger.getLogger("Plugin");
    
    public void init(Proxy context) throws IOException;
    public void read_handshake(Proxy context) throws IOException;
    public void send_handshake(Proxy context) throws IOException;
    public void read_auth(Proxy context) throws IOException;
    public void send_auth(Proxy context) throws IOException;
    public void read_auth_result(Proxy context) throws IOException;
    public void send_auth_result(Proxy context) throws IOException;
    public void read_query(Proxy context) throws IOException;
    public void send_query(Proxy context) throws IOException;
    public void read_query_result(Proxy context) throws IOException;
    public void send_query_result(Proxy context) throws IOException;
    public void cleanup(Proxy context) throws IOException;
}
