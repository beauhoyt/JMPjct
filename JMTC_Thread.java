import java.net.*;
import java.io.*;

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
            
            JMTC_Pipe Client_MySQL = new JMTC_Pipe(clientIn, mysqlOut);
            JMTC_Pipe MySQL_Client = new JMTC_Pipe(mysqlIn, clientOut);
     
            Client_MySQL.start();
            MySQL_Client.start();
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
