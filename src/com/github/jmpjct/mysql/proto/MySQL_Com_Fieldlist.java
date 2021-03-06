package com.github.jmpjct.mysql.proto;

/*
 * A MySQL Command Packet
 */

import java.util.ArrayList;
import org.apache.log4j.Logger;

public class MySQL_Com_Fieldlist extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL.Com.Fieldlist");
    
    public String table = "";
    public String fields = "";
    
    public ArrayList<byte[]> getPayload() {
        this.logger.trace("getPayload");
        ArrayList<byte[]> payload = new ArrayList<byte[]>();
        
        payload.add(MySQL_Proto.build_byte(MySQL_Flags.COM_FIELD_LIST));
        payload.add(MySQL_Proto.build_null_str(this.table));
        payload.add(MySQL_Proto.build_fixed_str(this.fields.length(), this.fields));
        
        return payload;
    }
}
