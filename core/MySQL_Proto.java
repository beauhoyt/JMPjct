/*
 * A collection of mysql proto based functions
 */

import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Proto {
    public static Logger logger = Logger.getLogger("MySQL_Proto");
    
    public static byte[] build_fixed_int(int size, long value) {
        byte[] packet = new byte[size];
        
        if (size == 8) {
            packet[0] = (byte) ((value >>  0) & 0xFF);
            packet[1] = (byte) ((value >>  8) & 0xFF);
            packet[2] = (byte) ((value >> 16) & 0xFF);
            packet[3] = (byte) ((value >> 24) & 0xFF);
            packet[4] = (byte) ((value >> 32) & 0xFF);
            packet[5] = (byte) ((value >> 40) & 0xFF);
            packet[6] = (byte) ((value >> 48) & 0xFF);
            packet[7] = (byte) ((value >> 56) & 0xFF);
        }
        else if (size == 4) {
            packet[0] = (byte) ((value >>  0) & 0xFF);
            packet[1] = (byte) ((value >>  8) & 0xFF);
            packet[2] = (byte) ((value >> 16) & 0xFF);
            packet[3] = (byte) ((value >> 24) & 0xFF);
        }
        else if (size == 3) {
            packet[0] = (byte) ((value >>  0) & 0xFF);
            packet[1] = (byte) ((value >>  8) & 0xFF);
            packet[2] = (byte) ((value >> 16) & 0xFF);
        }
        else if (size == 2) {
            packet[0] = (byte) ((value >>  0) & 0xFF);
            packet[1] = (byte) ((value >>  8) & 0xFF);
        }
        else if (size == 1) {
            packet[0] = (byte) ((value >>  0) & 0xFF);
        }
        else {
            MySQL_Proto.logger.fatal("Encoding int["+size+"] "+value+" failed!");
            return null;
        }
        return packet;
    }
    
    public static byte[] build_lenenc_int(long value) {
        byte[] packet = null;
        
        if (value < 251) {
            packet = new byte[1];
            packet[0] = (byte) ((value >>  0) & 0xFF);
        }
        else if (value < (2^16 - 1)) {
            packet = new byte[3];
            packet[0] = (byte) 0xFC;
            packet[1] = (byte) ((value >>  0) & 0xFF);
            packet[2] = (byte) ((value >>  8) & 0xFF);
        }
        else if (value < (2^24 - 1)) {
            packet = new byte[4];
            packet[0] = (byte) 0xFD;
            packet[1] = (byte) ((value >>  0) & 0xFF);
            packet[2] = (byte) ((value >>  8) & 0xFF);
            packet[3] = (byte) ((value >> 16) & 0xFF);
        }
        else {
            packet = new byte[9];
            packet[0] = (byte) 0xFE;
            packet[1] = (byte) ((value >>  0) & 0xFF);
            packet[2] = (byte) ((value >>  8) & 0xFF);
            packet[3] = (byte) ((value >> 16) & 0xFF);
            packet[4] = (byte) ((value >> 24) & 0xFF);
            packet[5] = (byte) ((value >> 32) & 0xFF);
            packet[6] = (byte) ((value >> 40) & 0xFF);
            packet[7] = (byte) ((value >> 48) & 0xFF);
            packet[8] = (byte) ((value >> 56) & 0xFF);
        }
        
        return packet;
    }
}
