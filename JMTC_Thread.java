import java.net.*;
import java.io.*;
import java.util.*;

public class JMTC_Thread extends Thread {
    private Socket socket = null;
    private String mysqlHost = null;
    private int mysqlPort;

    public JMTC_Thread(Socket socket, String mysqlHost, int mysqlPort) {
        this.socket = socket;
        this.mysqlHost = mysqlHost;
        this.mysqlPort = mysqlPort;
    }

    public void run() {
        try {
            InputStream clientIn = this.socket.getInputStream();
            OutputStream clientOut = this.socket.getOutputStream();
        
            Socket mysql = new Socket(this.mysqlHost, this.mysqlPort);
            InputStream mysqlIn = mysql.getInputStream();
            OutputStream mysqlOut = mysql.getOutputStream();
            
            while (true) {
                ArrayList<Integer> buffer = new ArrayList<Integer>();
                int b = 0;
                
                // Read from the client
                buffer.clear();
                while (clientIn.available() > 0) {
                    b = clientIn.read();
                    if (b == -1)
                        break;
                    buffer.add(b);
                    mysqlOut.write(b);
                }
                
                // Read from the server
                buffer.clear();
                while (mysqlIn.available() > 0) {
                    b = mysqlIn.read();
                    if (b == -1)
                        break;
                    buffer.add(b);
                    clientOut.write(b);
                }
            }
        }
        catch (IOException e) {
            return;
        }
    }
}
