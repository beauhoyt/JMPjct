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
import java.util.Properties;
import java.io.FileInputStream;

public class JMP {
    public static Properties config = new Properties();
    
    public static void main(String[] args) throws IOException {
        JMP.config.load(new FileInputStream(System.getProperty("config")));        
        
        ArrayList<JMP_Thread> threads = new ArrayList<JMP_Thread>();
        
        Logger logger = Logger.getLogger("JMP");
        PropertyConfigurator.configure(JMP.config.getProperty("logConf").trim());
        
        String[] ports = JMP.config.getProperty("ports").split(",");
        for (String port: ports) {
            JMP_Thread thread = new JMP_Thread(Integer.parseInt(port.trim()));
            thread.setName("Listener: "+port);
            thread.start();
            threads.add(thread);
        }
    }
}
