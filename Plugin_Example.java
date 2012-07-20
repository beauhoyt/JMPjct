import java.io.*;
import java.util.Date;

public class Plugin_Example extends Plugin_Base {
    public void init(Proxy context) {
        Date date = new Date();
        System.err.print("["+date+"]Plugin_Example->init\n");
    }
    
    public void read_handshake(Proxy context) {
        Date date = new Date();
        System.err.print("["+date+"]Plugin_Example->read_handshake\n");
    }
    
    public void read_auth(Proxy context) {
        Date date = new Date();
        System.err.print("["+date+"]Plugin_Example->read_auth\n");
    }
    
    public void read_auth_result(Proxy context) {
        Date date = new Date();
        System.err.print("["+date+"]Plugin_Example->read_auth_result\n");
    }
    
    public void read_query(Proxy context) {
        Date date = new Date();
        System.err.print("["+date+"]Plugin_Example->read_query\n");
    }
    
    public void read_query_result(Proxy context) {
        Date date = new Date();
        System.err.print("["+date+"]Plugin_Example->read_query_result\n");
    }
    
    public void send_query_result(Proxy context) {
        Date date = new Date();
        System.err.print("["+date+"]Plugin_Example->send_query_result\n");
    }
    
    public void cleanup(Proxy context) {
        Date date = new Date();
        System.err.print("["+date+"]Plugin_Example->cleanup\n");
    }
    
}
