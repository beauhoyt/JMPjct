/*
 * A MySQL Row Packet
 */

import java.util.*;
import org.apache.log4j.Logger;

public class MySQL_Row extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Row");
    
    public ArrayList<Object> data = new ArrayList<Object>();
    
    public void addData(String data) {
        this.data.add(data);
    }
    
    public void addData(Integer data) {
        this.data.add(Integer.toString(data));
    }
    
    public void addData(long data) {
        this.data.add(String.valueOf(data));
    }
    
    // Add other addData for other types here
    
    public byte[] getPayload() {
        int size = 0;
        
        for (Object obj: this.data) {
            if (obj instanceof String){
                byte[] dataBytes = MySQL_Proto.build_lenenc_str((String)obj);
                size += dataBytes.length;
            }
            else if (obj instanceof Integer || obj == null) {
                byte[] dataBytes = MySQL_Proto.build_lenenc_int((Integer)obj);
                size += dataBytes.length;
            }
            else {
                // trigger error
            }
        }
        
        byte[] packet = new byte[size];
        
        int offset = 0;
        
        for (Object obj: this.data) {
            if (obj instanceof String){
                byte[] dataBytes = MySQL_Proto.build_lenenc_str((String)obj);
                System.arraycopy(dataBytes, 0, packet, offset, dataBytes.length);
                offset += dataBytes.length;
            }
            else if (obj instanceof Integer || obj == null) {
                byte[] dataBytes = MySQL_Proto.build_lenenc_int((Integer)obj);
                System.arraycopy(dataBytes, 0, packet, offset, dataBytes.length);
                offset += dataBytes.length;
            }
        }
        
        return packet;
    }
}
