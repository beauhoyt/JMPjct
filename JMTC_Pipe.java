import java.net.*;
import java.io.*;

public class JMTC_Pipe extends Thread implements Runnable{
 
    private InputStream in = null;
    private OutputStream out = null;
 
    JMTC_Pipe (InputStream in, OutputStream out) {
        this.in = in;
        this.out = out; 
    }
 
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes_read = 0;
        try {
            do {
                bytes_read = in.read(buffer);
                if (bytes_read > 0)
                    out.write(buffer, 0, bytes_read);
            } while (bytes_read != -1);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
} 
