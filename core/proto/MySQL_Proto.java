/*
 * A collection of mysql proto based functions
 */

import org.apache.log4j.Logger;

public class MySQL_Proto {
    public static Logger logger = Logger.getLogger("MySQL.Proto");
    
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
    
    public static byte[] build_lenenc_str(String str) {
        if (str.equals("")) {
            byte[] packet = new byte[1];
            packet[0] = 0x00;
            return packet;
        }
        
        byte[] size = MySQL_Proto.build_lenenc_int(str.length());
        byte[] strByte = MySQL_Proto.build_fixed_str(str.length(), str);
        byte[] packet = new byte[size.length + strByte.length];
        System.arraycopy(size, 0, packet, 0, size.length);
        System.arraycopy(strByte, 0, packet, size.length, strByte.length);
        return packet;
    }
    
    public static byte[] build_null_str(String str) {
        return MySQL_Proto.build_fixed_str(str.length() + 1, str);
    }
    
    public static byte[] build_fixed_str(int size, String str) {
        byte[] packet = new byte[size];
        byte[] strByte = str.getBytes();
        if (strByte.length < packet.length)
            size = strByte.length;
        System.arraycopy(strByte, 0, packet, 0, size);
        return packet;
    }
    
    public static byte[] build_filler(int len) {
        byte[] filler = new byte[len];
        for (int i = 0; i < len; i++)
            filler[i] = 0x00;
        return filler;
    }
    
    public static byte[] build_byte(byte value) {
        byte[] field = new byte[1];
        field[0] = value;
        return field;
    }
    
    public static char int2char(byte i) {
        return (char)i;
    }
    
    public static byte char2int(char i) {
        return (byte)i;
    }
}
