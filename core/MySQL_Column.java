/*
 * A MySQL Column Packet
 */

import org.apache.log4j.Logger;

public class MySQL_Column extends MySQL_Packet {
    public Logger logger = Logger.getLogger("MySQL_Column");
    
    public String catalog = "def";
    public String schema = "";
    public String table = "";
    public String org_table = "";
    public String name = "";
    public String org_name = "";
    public long characterSet = 0;
    public long columnLength = 0;
    public long type = MySQL_Flags.MYSQL_TYPE_VAR_STRING;
    public long flags = 0;
    public long decimals = 31;
    
    public MySQL_Column(String name) {
        // Set this up by default. Allow overrides if needed
        this.characterSet = MySQL_ResultSet_Text.characterSet;
        this.name = name;
    }
    
    public byte[] getPayload() {
        int size = 0;
        
        byte[] catalog = MySQL_Proto.build_lenenc_str(this.catalog);
        size += catalog.length;
        
        byte[] schema = MySQL_Proto.build_lenenc_str(this.schema);
        size += schema.length;
        
        byte[] table = MySQL_Proto.build_lenenc_str(this.table);
        size += table.length;
        
        byte[] org_table = MySQL_Proto.build_lenenc_str(this.org_table);
        size += org_table.length;
        
        byte[] name = MySQL_Proto.build_lenenc_str(this.name);
        size += name.length;
        
        byte[] org_name = MySQL_Proto.build_lenenc_str(this.org_name);
        size += org_name.length;
        
        byte[] filter1 = new byte[1];
        filter1[0] = 0x00;
        size += filter1.length;
        
        byte[] characterSet = MySQL_Proto.build_fixed_int(2, this.characterSet);
        size += characterSet.length;
        
        byte[] columnLength = MySQL_Proto.build_fixed_int(4, this.columnLength);
        size += columnLength.length;
        
        byte[] type = MySQL_Proto.build_fixed_int(1, this.type);
        size += type.length;
        
        byte[] flags = MySQL_Proto.build_fixed_int(2, this.flags);
        size += flags.length;
        
        byte[] decimals = MySQL_Proto.build_fixed_int(1, this.decimals);
        size += decimals.length;
        
        byte[] filter2 = new byte[2];
        filter2[0] = 0x00;
        filter2[1] = 0x00;
        size += filter2.length;
        
        byte[] packet = new byte[size];
        
        int offset = 0;
        
        System.arraycopy(catalog, 0, packet, offset, catalog.length);
        offset += catalog.length;
        
        System.arraycopy(schema, 0, packet, offset, schema.length);
        offset += schema.length;
        
        System.arraycopy(table, 0, packet, offset, table.length);
        offset += table.length;
        
        System.arraycopy(org_table, 0, packet, offset, org_table.length);
        offset += org_table.length;
        
        System.arraycopy(name, 0, packet, offset, name.length);
        offset += name.length;
        
        System.arraycopy(org_name, 0, packet, offset, org_name.length);
        offset += org_name.length;
        
        System.arraycopy(filter1, 0, packet, offset, filter1.length);
        offset += filter1.length;
        
        System.arraycopy(characterSet, 0, packet, offset, characterSet.length);
        offset += characterSet.length;
        
        System.arraycopy(columnLength, 0, packet, offset, columnLength.length);
        offset += columnLength.length;
        
        System.arraycopy(type, 0, packet, offset, type.length);
        offset += type.length;
        
        System.arraycopy(flags, 0, packet, offset, flags.length);
        offset += flags.length;
        
        System.arraycopy(decimals, 0, packet, offset, decimals.length);
        offset += decimals.length;
        
        System.arraycopy(filter2, 0, packet, offset, filter2.length);
        offset += filter2.length;
        
        return packet;
    }
}
