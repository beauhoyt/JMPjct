/*
 * A collection of mysql proto based functions
 */

import org.apache.log4j.Logger;

public class MySQL_Proto {
    public static Logger logger = Logger.getLogger("MySQL.Proto");
    public static int offsetOffset = 0;
    
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
    
    public static int get_offset_offset() {
        int offsetOffset = MySQL_Proto.offsetOffset;
        MySQL_Proto.offsetOffset = 0;
        return offsetOffset;
    }
    
    public static long get_fixed_int(byte[] packet, int offset, int size) {
        byte[] bytes = null;
        
        if ( packet.length < (size + offset))
            return -1;
        
        bytes = new byte[size];
        System.arraycopy(packet, offset, bytes, 0, size);
        MySQL_Proto.offsetOffset = size;
        return MySQL_Proto.get_fixed_int(bytes);
    }
    
    public static long get_fixed_int(byte[] bytes) {
        long value = 0;
        
        for (int i = bytes.length-1; i > 0; i--) {
            value |= bytes[i] & 0xFF;
            value <<= 8;
        }
        value |= bytes[0] & 0xFF;
                  
        return value;
    }
    
    public static long get_lenenc_int(byte[] packet, int offset) {
        int size = 0;
        
        // 1 byte int
        if (packet[offset] < 251) {
            size = 1;
        }
        // 2 byte int
        else if (packet[offset] == 252) {
            MySQL_Proto.offsetOffset += 1;
            size = 2;
        }
        // 3 byte int
        else if (packet[offset] == 253) {
            MySQL_Proto.offsetOffset += 1;
            size = 3;
        }
        // 8 byte int
        else if (packet[offset] == 254) {
            MySQL_Proto.offsetOffset += 1;
            size = 8;
        }
        
        if (size == 0) {
            MySQL_Proto.logger.fatal("Decoding int at offset "+offset+" failed!");
            return -1;
        }
        
        return MySQL_Proto.get_fixed_int(packet, offset, size);
    }
    
    public static String get_fixed_str(byte[] packet, int offset, int len) {
        String str = "";
        
        for (int i = offset; i < (offset+len); i++) {
            str += MySQL_Proto.int2char(packet[i]);
            MySQL_Proto.offsetOffset += 1;
        }
        
        return str;
    }
    
    public static String get_null_str(byte[] packet, int offset) {
        String str = "";
        
        for (int i = offset; i < packet.length; i++) {
            if (packet[i] == 0x00) {
                MySQL_Proto.offsetOffset += 1;
                break;
            }
            str += MySQL_Proto.int2char(packet[i]);
            MySQL_Proto.offsetOffset += 1;
        }
        
        return str;
    }
    
    public static String get_eop_str(byte[] packet, int offset) {
        String str = "";
        
        for (int i = offset; i < packet.length; i++) {
            if (packet[i] == 0x00 && i == packet.length-1) {
                MySQL_Proto.offsetOffset += 1;
                break;
            }
            str += MySQL_Proto.int2char(packet[i]);
            MySQL_Proto.offsetOffset += 1;
        }
        
        return str;
    }
    
    public static String get_lenenc_str(byte[] packet, int offset) {
        String str = "";
        int i = 0;
        int offsetOffset = 0;
        int size = (int)MySQL_Proto.get_lenenc_int(packet, offset);
        
        offsetOffset = MySQL_Proto.get_offset_offset();
        offset += offsetOffset;
        
        for (i = offset; i < (offset + size); i++) {
            str += MySQL_Proto.int2char(packet[i]);
            MySQL_Proto.offsetOffset += 1;
        }
        MySQL_Proto.offsetOffset += offsetOffset;
        
        return str;
    }
}
