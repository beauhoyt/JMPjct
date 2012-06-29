import java.io.*;
import java.net.*;
import java.net.ServerSocket;

public class JMTC {
    public static void main(String[] args) throws IOException {
        String mysqlHost = System.getProperty("mysqlHost");
        int mysqlPort = Integer.parseInt(System.getProperty("mysqlPort"));
        int port = Integer.parseInt(System.getProperty("port"));
        boolean listening = true;
        ServerSocket listener = null;
        
        try {
            listener = new ServerSocket(port);
        }
        catch (IOException e) {
            System.out.println("Could not listen on port");
            System.exit(-1);
        }
        
        while (listening)
            new JMTC_Thread(listener.accept(), mysqlHost, mysqlPort).start();
 
        listener.close();
    }
}
