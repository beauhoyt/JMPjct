/*
 * Example plugin. Just log timing information and hook names
 */

import java.io.*;
import java.util.Date;
import org.apache.log4j.Logger;

public class Plugin_Example extends Plugin_Base {
    public Logger logger = Logger.getLogger("Plugin.Base");
    
    public void init(Proxy context) {
        this.logger.info("Plugin_Example->init");
    }
    
    public void read_handshake(Proxy context) {
        this.logger.info("Plugin_Example->read_handshake");
    }
    
    public void read_auth(Proxy context) {
        this.logger.info("Plugin_Example->read_auth");
    }
    
    public void read_auth_result(Proxy context) {
        this.logger.info("Plugin_Example->read_auth_result");
    }
    
    public void read_query(Proxy context) {
        this.logger.info("Plugin_Example->read_query");
    }
    
    public void read_query_result(Proxy context) {
        this.logger.info("Plugin_Example->read_query_result");
    }
    
    public void send_query_result(Proxy context) {
        this.logger.info("Plugin_Example->send_query_result");
    }
    
    public void cleanup(Proxy context) {
        this.logger.info("Plugin_Example->cleanup");
    }
    
}
