package com.github.jmpjct.mysql.proto;

/*
 * A MySQL Row Packet
 */

import java.util.ArrayList;
import org.apache.log4j.Logger;
import com.github.jmpjct.mysql.proto.MySQL_Packet;
import com.github.jmpjct.mysql.proto.MySQL_Flags;

public class MySQL_Row extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Row");
    
    public int type = MySQL_Flags.ROW_TYPE_TEXT;
    public int colType = MySQL_Flags.MYSQL_TYPE_VAR_STRING;
    public ArrayList<Object> data = new ArrayList<Object>();
    
    public MySQL_Row () {}
    
    public MySQL_Row (String data1) {
        this.addData(data1);
    }
    
    public void addData(String data) {
        this.data.add(data);
    }
    
    public MySQL_Row (String data1, Integer data2) {
        this.addData(data1);
        this.addData(data2);
    }
    
    public MySQL_Row (String data1, long data2) {
        this.addData(data1);
        this.addData(data2);
    }
    
    public MySQL_Row (String data1, float data2) {
        this.addData(data1);
        this.addData(data2);
    }
    
    public MySQL_Row (String data1, boolean data2) {
        this.addData(data1);
        this.addData(data2);
    }
    
    public MySQL_Row (String data1, String data2) {
        this.addData(data1);
        this.addData(data2);
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
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        for (Object obj: this.data) {
            switch (this.type) {
                case MySQL_Flags.ROW_TYPE_TEXT: 
                    if (obj instanceof String)
                        payload.add(MySQL_Proto.build_lenenc_str((String)obj));
                    else if (obj instanceof Integer || obj == null)
                        payload.add(MySQL_Proto.build_lenenc_int((Integer)obj));
                    else {
                        // trigger error
                    }
                    break;
                case MySQL_Flags.ROW_TYPE_BINARY:
                    break;
                default:
                    break;
            }
        }
        
        return payload;
    }
}
