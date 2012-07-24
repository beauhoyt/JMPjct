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
    boolean listening = true;
    ServerSocket listener = null;
    ArrayList<Plugin_Base> plugins = new ArrayList<Plugin_Base>();
    Logger logger = Logger.getLogger("JMP_Thread");
    
    public JMP_Thread(int port) {
        this.port = port;
    }
    
    public void run() {
        try {
            this.listener = new ServerSocket(this.port);
        }
        catch (IOException e) {
            this.logger.fatal("Could not listen on port "+this.port);
            System.exit(-1);
        }
        
        this.logger.info("Listening on "+this.port);
        
        String[] ps = new String[0];
        
        if (System.getProperty("plugins") != null)
            ps = System.getProperty("plugins").split(",");
        
        while (this.listening) {
            plugins = new ArrayList<Plugin_Base>();
            for (String p: ps) {
                try {
                    plugins.add((Plugin_Base) Plugin_Base.class.getClassLoader().loadClass(p).newInstance());
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
                new Engine(this.port, this.listener.accept(), plugins).start();
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
