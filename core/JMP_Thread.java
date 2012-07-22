/*
 * Java Mysql Proxy
 * Main binary. Just listen for connections and pass them over
 * to the proxy module
 */

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import org.apache.log4j.Logger;

public class JMP_Thread extends Thread {
    int port;
    String mysqlHost;
    int mysqlPort;
    boolean listening = true;
    ServerSocket listener = null;
    ArrayList<Proxy_Plugin> plugins = new ArrayList<Proxy_Plugin>();
    Logger logger = Logger.getLogger("JMP_Thread");
    
    public JMP_Thread(int port, String mysqlHost, int mysqlPort) {
        this.port = port;
        this.mysqlHost = mysqlHost;
        this.mysqlPort = mysqlPort;
    }
    
    public void run() {
        try {
            this.listener = new ServerSocket(this.port);
        }
        catch (IOException e) {
            this.logger.fatal("Could not listen on port "+this.port);
            System.exit(-1);
        }
        
        this.logger.info("Listening on "+this.port+" and forwarding to "+this.mysqlHost+":"+this.mysqlPort);
        
        String[] ps = System.getProperty("plugins").split(",");
        
        while (this.listening) {
            plugins = new ArrayList<Proxy_Plugin>();
            for (String p: ps) {
                try {
                    plugins.add((Proxy_Plugin) Proxy_Plugin.class.getClassLoader().loadClass(p).newInstance());
                    this.logger.info("Loaded plugin "+p);
                }
                catch (java.lang.ClassNotFoundException e) {
                    this.logger.error("Failed to load plugin "+p);
                    continue;
                }
                catch (java.lang.InstantiationException e) {
                    this.logger.error("Failed to load plugin "+p);
                    continue;
                }
                catch (java.lang.IllegalAccessException e) {
                    this.logger.error("Failed to load plugin "+p);
                    continue;
                }
            }
            try {
                new Proxy(this.listener.accept(), this.mysqlHost, this.mysqlPort, plugins).start();
            }
            catch (java.io.IOException e) {
                this.logger.fatal("Accept fatal "+e);
                this.listening = false;
            }
        }
    
        try {
            this.listener.close();
        }
        catch (java.io.IOException e) {}
    }
}
