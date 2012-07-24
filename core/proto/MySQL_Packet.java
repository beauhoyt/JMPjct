/*
 * A MySQL Packet
 */

import java.util.*;
import java.io.*;
import org.apache.commons.io.*;
import org.apache.log4j.Logger;

public abstract class MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Packet");
    
    public long sequenceId = 0;
    
    public abstract ArrayList<byte[]> getPayload();
    
    public byte[] toPacket() {
        this.logger.trace("toPacket");
        ArrayList<byte[]> payload = this.getPayload();
        
        int size = 0;
        for (byte[] field: payload)
            size += field.length;
        
        byte[] packet = new byte[size+4];
        
        System.arraycopy(MySQL_Proto.build_fixed_int(3, size), 0, packet, 0, 3);
        System.arraycopy(MySQL_Proto.build_fixed_int(1, this.sequenceId), 0, packet, 3, 1);
        
        int offset = 4;
        for (byte[] field: payload) {
            System.arraycopy(field, 0, packet, offset, field.length);
            offset += field.length;
        }
        
        return packet;
    }
    
    public static int getSize(byte[] packet) {
        Logger.getLogger("MySQL.Packet").trace("getSize");
        int size = (int)MySQL_Proto.get_fixed_int(packet, 0, 3);
        MySQL_Proto.get_offset_offset();
        Logger.getLogger("MySQL.Packet").trace("Packet size is "+size);
        return size;
    }
    
    public static byte getType(byte[] packet) {
        Logger.getLogger("MySQL.Packet").trace("getType");
        return packet[4];
    }
    
    public static long getSequenceId(byte[] packet) {
        Logger.getLogger("MySQL.Packet").trace("getSequenceId");
        return MySQL_Proto.get_fixed_int(packet, 3, 1);
    }
    
    public static final void dump(byte[] packet) {
        Logger logger = Logger.getLogger("MySQL.Packet");
        
        if (!logger.isTraceEnabled())
            return;
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HexDump.dump(packet, 0, out, 0);
            logger.trace("Dumping packet\n"+out.toString());
        }
        catch (IOException e) {
            return;
        }
    }
    
    public static byte[] read_packet(InputStream in) throws IOException {
        Logger.getLogger("MySQL.Packet").trace("read_packet");
        int b = 0;
        int size = 0;
        byte[] packet = new byte[3];
        
        // Read size (3)
        int offset = 0;
        int target = 3;
        do {
            b = in.read(packet, offset, (target - offset));
            if (b == -1) {
                throw new IOException();
            }
            offset += b;
        } while (offset != target);
        
        size = MySQL_Packet.getSize(packet);
        
        byte[] packet_tmp = new byte[size+4];
        System.arraycopy(packet, 0, packet_tmp, 0, 3);
        packet = packet_tmp;
        packet_tmp = null;
        
        target = packet.length;
        do {
            b = in.read(packet, offset, (target - offset));
            if (b == -1) {
                throw new IOException();
            }
            offset += b;
        } while (offset != target);
        
        MySQL_Packet.dump(packet);
        return packet;
    }
    
    public static ArrayList<byte[]> read_full_result_set(InputStream in, OutputStream out, ArrayList<byte[]> buffer, boolean bufferResultSet) throws IOException {
        Logger logger = Logger.getLogger("MySQL.Packet");
        logger.trace("read_full_result_set");
        // Assume we have the start of a result set already
        
        byte[] packet = buffer.get((buffer.size()-1));
        long colCount = MySQL_ColCount.loadFromPacket(packet).colCount;
        logger.trace("colCount "+colCount);
        
        // Read the columns and the EOF field
        for (int i = 0; i < (colCount+1); i++) {
            logger.trace("Reading col "+i);
            
            // Evil optimization
            if (!bufferResultSet) {
                MySQL_Packet.write(out, buffer);
                buffer = new ArrayList<byte[]>();
            }
                
            packet = MySQL_Packet.read_packet(in);
            if (packet == null) {
                throw new IOException();
            }
            buffer.add(packet);
        }
        
        do {
            logger.trace("Reading row");
            // Evil optimization
            if (!bufferResultSet) {
                MySQL_Packet.write(out, buffer);
                buffer = new ArrayList<byte[]>();
            }
            
            packet = MySQL_Packet.read_packet(in);
            if (packet == null) {
                throw new IOException();
            }
            buffer.add(packet);
        } while (MySQL_Packet.getType(packet) != MySQL_Flags.EOF && MySQL_Packet.getType(packet) != MySQL_Flags.ERR);
        
        // Evil optimization
        if (!bufferResultSet) {
            MySQL_Packet.write(out, buffer);
            buffer = new ArrayList<byte[]>();
        }
        
        if (MySQL_Packet.getType(packet) == MySQL_Flags.ERR)
            return buffer;
        
        if (MySQL_EOF.loadFromPacket(packet).hasStatusFlag(MySQL_Flags.SERVER_MORE_RESULTS_EXISTS)) {
            logger.trace("More Result Sets.");
            buffer.add(MySQL_Packet.read_packet(in));
            buffer = MySQL_Packet.read_full_result_set(in, out, buffer, bufferResultSet);
        }
        Plugin_Debug.dump_buffer(buffer);
        return buffer;
    }
    
    public static void write(OutputStream out, ArrayList<byte[]> buffer) throws IOException {
        Logger.getLogger("MySQL.Packet").trace("write");
        
        for (byte[] packet: buffer) {
            Logger.getLogger("MySQL.Packet").trace("Writing packet size "+packet.length);
            MySQL_Packet.dump(packet);
            out.write(packet);
        }
    }
    
}
