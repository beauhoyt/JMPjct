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
    
    public void addData(float data) {
        this.data.add(String.valueOf(data));
    }
    
    public void addData(boolean data) {
        this.data.add(String.valueOf(data));
    }
    
    // Add other addData for other types here
    
    public ArrayList<byte[]> getPayload() {
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        for (Object obj: this.data) {
            if (obj instanceof String)
                payload.add(MySQL_Proto.build_lenenc_str((String)obj));
            else if (obj instanceof Integer || obj == null)
                payload.add(MySQL_Proto.build_lenenc_int((Integer)obj));
            else {
                // trigger error
            }
        }
        
        return payload;
    }
}
