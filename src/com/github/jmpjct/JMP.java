package com.github.jmpjct;

/*
 * Java Mysql Proxy
 * Main binary. Just listen for connections and pass them over
 * to the proxy module
 */

import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class JMP {
    public static void main(String[] args) throws IOException {
        ArrayList<JMP_Thread> threads = new ArrayList<JMP_Thread>();
        
        Logger logger = Logger.getLogger("JMP");
        PropertyConfigurator.configure(System.getProperty("logConf"));
        
        String[] ports = System.getProperty("ports").split(",");
        for (String port: ports) {
            JMP_Thread thread = new JMP_Thread(Integer.parseInt(port));
            thread.setName("Listener: "+port);
            thread.start();
            threads.add(thread);
        }
    }
}
