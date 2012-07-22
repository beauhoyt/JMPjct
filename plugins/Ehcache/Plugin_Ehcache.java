import java.util.*;
import org.apache.log4j.Logger;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class Plugin_Ehcache extends Plugin_Base {
    private static Ehcache cache = null;
    private static CacheManager cachemanager = null;
    
    public Logger logger = Logger.getLogger("Plugin.Ehcache");
    private int TTL = 0;
    private String key = "";
    
    public void init(Proxy context) {
        // Key off of the remote mysql host
        String cacheName = context.mysqlHost+":"+context.mysqlPort;
        
        if (Plugin_Ehcache.cachemanager == null) {
            Plugin_Ehcache.cachemanager = CacheManager.create("conf/ehcache.xml");
        }
        
        if (Plugin_Ehcache.cache == null) {
            Plugin_Ehcache.cache = Plugin_Ehcache.cachemanager.getEhcache(cacheName);
        }
        
        if (Plugin_Ehcache.cache == null) {
            this.logger.fatal("Ehcache is null! Does instance '"+cacheName+"' exist?");
            context.halt();
        }
    }
    
    @SuppressWarnings("unchecked")
    public void read_query(Proxy context) {
        String query = context.query;
        String command = "";
        String value = "";
        
        // Reset all values on a new query
        this.TTL = 0;
        this.key = "";
        context.bufferResultSet = false;
        
        if (!query.startsWith("/* "))
            return;
        
        // Extract out the command
        command = query.substring(3, query.indexOf("*/")).trim();
        
        if (command.indexOf(":") != -1) {
            value = command.substring(command.indexOf(":")+1).trim();
            command = command.substring(0, command.indexOf(":")).trim();
        }
        
        query = query.substring(query.indexOf("*/")+2).trim();
        this.key = context.schema+":"+query;
        
        this.logger.info("Cache Key: '"+this.key+"'");
        this.logger.trace("Command: '"+command+"'"+" value: '"+value+"'");
        
        if (command.equals("CACHE")) {
            this.logger.trace("CACHE");
            this.TTL = Integer.parseInt(value);
            context.bufferResultSet = true;
            
            Element element = Plugin_Ehcache.cache.get(this.key);
            
            if (element != null) {
                this.logger.trace("Cache Hit!");
                
                context.clear_buffer();
                context.buffer = (ArrayList<byte[]>) element.getValue();
                context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
                
                if (context.buffer.size() == 0) {
                    MySQL_ERR err = new MySQL_ERR();
                    err.sequenceId = context.sequenceId+1;
                    err.errorCode = 1032;
                    err.sqlState = "HY000";
                    err.errorMessage = "Can't find record in ehcache";
                    
                    context.clear_buffer();
                    context.buffer.add(err.toPacket());
                    context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
                    
                    this.logger.fatal("Cache hit but invalid result!");
                }
            }
        }
        else if (command.equals("FLUSH")) {
            this.logger.trace("FLUSH");
            MySQL_OK ok = new MySQL_OK();
            
            boolean removed = Plugin_Ehcache.cache.remove(this.key);
            if (removed)
                ok.affectedRows = 1;
            ok.sequenceId = context.sequenceId+1;
            
            context.clear_buffer();
            context.buffer.add(ok.toPacket());
            context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
        }
        else if (command.equals("REFRESH")) {
            this.logger.trace("REFRESH");
            Plugin_Ehcache.cache.remove(this.key);
            this.TTL = Integer.parseInt(value);
            context.bufferResultSet = true;
        }
        else if (command.equals("STATS")) {
            this.logger.trace("STATS");
            MySQL_ResultSet_Text rs = new MySQL_ResultSet_Text();
            MySQL_Column key = new MySQL_Column("Key");
            rs.addColumn(key);
            MySQL_Column val = new MySQL_Column("Value");
            rs.addColumn(val);
            
            MySQL_Row row = new MySQL_Row();
            row.addData("Elements in Cache");
            row.addData(Plugin_Ehcache.cache.getSize());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("Elements in Memory");
            row.addData(Plugin_Ehcache.cache.getMemoryStoreSize());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("Elements on Disk");
            row.addData(Plugin_Ehcache.cache.getDiskStoreSize());
            rs.addRow(row);
            
            context.clear_buffer();
            context.buffer = rs.toPackets();
            context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
        }
        else if (command.equals("DUMP KEYS")) {
            this.logger.trace("DUMP KEYS");
            List keys = this.cache.getKeysWithExpiryCheck();
            
            MySQL_ResultSet_Text rs = new MySQL_ResultSet_Text();
            MySQL_Column key = new MySQL_Column("Key");
            rs.addColumn(key);
            
            for (Object k: keys) {
                this.logger.trace("Key: '"+k+"'");
                MySQL_Row row = new MySQL_Row();
                row.addData(k.toString());
                rs.addRow(row); 
            }
            
            context.clear_buffer();
            context.buffer = rs.toPackets();
            context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
        }
        else {
            this.logger.trace("FAIL");
            MySQL_ERR err = new MySQL_ERR();
            err.sequenceId = context.sequenceId+1;
            err.errorCode = 1047;
            err.sqlState = "08S01";
            err.errorMessage = "Unknown command '"+command+"'";
            
            context.clear_buffer();
            context.buffer.add(err.toPacket());
            context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
            
            this.logger.fatal(command+" is unknown!");
        }
    }
    
    public void read_query_result(Proxy context) {
        // Cache this key?
        if (this.TTL == 0 || context.buffer.size() == 0)
            return;
        
        Element element = new Element(this.key, context.buffer);
        element.setTimeToLive(this.TTL);
        Plugin_Ehcache.cache.put(element);
    }
}
