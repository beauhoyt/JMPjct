package com.github.jmpjct.plugin;

/*
 * Create an empty abstract class to allow plugins to only
 * implement their required differences.
 */
import org.apache.log4j.Logger;
import java.io.IOException;
import com.github.jmpjct.Engine;

public abstract class Base {
    public Logger logger = Logger.getLogger("Plugin.Base");
    
    public void init(Engine context) throws IOException {}
    
    public void read_handshake(Engine context) throws IOException {}
    public void send_handshake(Engine context) throws IOException {}
    
    public void read_auth(Engine context) throws IOException {}
    public void send_auth(Engine context) throws IOException {}
    
    public void read_auth_result(Engine context) throws IOException {}
    public void send_auth_result(Engine context) throws IOException {}
    
    public void read_query(Engine context) throws IOException {}
    public void send_query(Engine context) throws IOException {}
    
    public void read_query_result(Engine context) throws IOException {}
    public void send_query_result(Engine context) throws IOException {}
    
    public void cleanup(Engine context) throws IOException {}
}
