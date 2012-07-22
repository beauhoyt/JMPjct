/*
 * Java Mysql Proxy
 * Main binary. Just listen for connections and pass them over
 * to the proxy module
 */

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class JMP {
    public static void main(String[] args) throws IOException {
        ArrayList<JMP_Thread> threads = new ArrayList<JMP_Thread>();
        
        Logger logger = Logger.getLogger("JMP");
        PropertyConfigurator.configure(System.getProperty("logConf"));
        
        String[] hosts = System.getProperty("Hosts").split(",");
        for (String host: hosts) {
            String[] hostInfo = host.split(":");
            
            int port = Integer.parseInt(hostInfo[0]);
            String mysqlHost = hostInfo[1];
            int mysqlPort = Integer.parseInt(hostInfo[2]);
            
            JMP_Thread thread = new JMP_Thread(port, mysqlHost, mysqlPort);
            thread.setName("Listener: "+port);
            thread.start();
            threads.add(thread);
        }
    }
}
