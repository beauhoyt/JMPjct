package com.github.jmpjct.plugin.example;

/*
 * Example plugin. Just log timing information and hook names
 */

import java.util.Date;
import org.apache.log4j.Logger;
import com.github.jmpjct.plugin.Base;
import com.github.jmpjct.Engine;

public class Example extends Base {
    public Logger logger = Logger.getLogger("Plugin.Base");
    
    public void init(Engine context) {
        this.logger.info("Plugin_Example->init");
    }
    
    public void read_handshake(Engine context) {
        this.logger.info("Plugin_Example->read_handshake");
    }
    
    public void read_auth(Engine context) {
        this.logger.info("Plugin_Example->read_auth");
    }
    
    public void read_auth_result(Engine context) {
        this.logger.info("Plugin_Example->read_auth_result");
    }
    
    public void read_query(Engine context) {
        this.logger.info("Plugin_Example->read_query");
    }
    
    public void read_query_result(Engine context) {
        this.logger.info("Plugin_Example->read_query_result");
    }
    
    public void send_query_result(Engine context) {
        this.logger.info("Plugin_Example->send_query_result");
    }
    
    public void cleanup(Engine context) {
        this.logger.info("Plugin_Example->cleanup");
    }
    
}
