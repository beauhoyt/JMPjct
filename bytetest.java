import java.io.*;
import java.net.*;
import java.util.*;
import java.net.ServerSocket;

public class bytetest {
    public static void main(String[] args) throws IOException {
        
        byte[] b = new byte[3];
        b[0] = (byte)0x34;
        b[1] = (byte)0x00;
        b[2] = (byte)0x00;
        
        long l = 0;
        l |= b[2] & 0xFF;
        l <<= 8;
        l |= b[1] & 0xFF;
        l <<= 8;
        l |= b[0] & 0xFF;
        
        System.err.print("int l "+l+"\n");
    }
}
