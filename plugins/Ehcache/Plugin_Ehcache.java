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
        // Bail out early if we have a cache
        if (Plugin_Ehcache.cache != null)
            return;
            
        if (Plugin_Ehcache.cachemanager == null) {
            this.logger.trace("Ehcache - CacheManager: Loading "+System.getProperty("ehcacheConf"));
            Plugin_Ehcache.cachemanager = CacheManager.create(System.getProperty("ehcacheConf"));
        }
        
        if (Plugin_Ehcache.cache == null) {
            this.logger.trace("Ehcache - cache: Getting "+System.getProperty("ehcacheCacheName"));
            Plugin_Ehcache.cache = Plugin_Ehcache.cachemanager.getEhcache(System.getProperty("ehcacheCacheName"));
        }
        
        if (Plugin_Ehcache.cache == null) {
            this.logger.fatal("Ehcache is null! Does instance '"+System.getProperty("ehcacheCacheName")+"' exist?");
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
        this.key = context.mysqlHost+":"+context.mysqlPort+"/"+context.schema+"/"+query;
        
        this.logger.info("Cache Key: '"+this.key+"'");
        this.logger.trace("Command: '"+command+"'"+" value: '"+value+"'");
        
        if (command.equalsIgnoreCase("CACHE")) {
            this.logger.trace("CACHE");
            this.TTL = Integer.parseInt(value);
            context.bufferResultSet = true;
            
            Plugin_Ehcache.cache.acquireWriteLockOnKey(this.key);
            
            Element element = Plugin_Ehcache.cache.get(this.key);
            
            if (element != null) {
                this.logger.trace("Cache Hit!");
                Plugin_Ehcache.cache.releaseWriteLockOnKey(this.key);
                
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
        else if (command.equalsIgnoreCase("FLUSH")) {
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
        else if (command.equalsIgnoreCase("FLUSHALL")) {
            this.logger.trace("FLUSHALL");
            MySQL_OK ok = new MySQL_OK();
            
            Plugin_Ehcache.cache.removeAll();
            ok.sequenceId = context.sequenceId+1;
            
            context.clear_buffer();
            context.buffer.add(ok.toPacket());
            context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
        }
        else if (command.equalsIgnoreCase("REFRESH")) {
            this.logger.trace("REFRESH");
            Plugin_Ehcache.cache.remove(this.key);
            this.TTL = Integer.parseInt(value);
            context.bufferResultSet = true;
        }
        else if (command.equalsIgnoreCase("STATS")) {
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
        else if (command.equalsIgnoreCase("DUMP KEYS")) {
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
        else if (command.equalsIgnoreCase("EHCACHE HELP")) {
            this.logger.trace("EHCACHE HELP");
            
            MySQL_ResultSet_Text rs = new MySQL_ResultSet_Text();
            
            rs.addColumn(new MySQL_Column("Command"));
            rs.addColumn(new MySQL_Column("Help"));
            
            MySQL_Row row = null;
            
            row = new MySQL_Row();
            row.addData("Usage");
            row.addData("/* COMMAND */ SELECT query FROM table");
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("CACHE: TTL");
            row.addData("Cache the query result for TTL seconds");
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("FLUSH");
            row.addData("Flush the query from the cache");
            rs.addRow(row); 
            
            row = new MySQL_Row();
            row.addData("REFRESH: TTL");
            row.addData("Expire the query and refetch it");
            rs.addRow(row); 
            
            row = new MySQL_Row();
            row.addData("STATS");
            row.addData("Output Ehcache statistics");
            rs.addRow(row); 
            
            row = new MySQL_Row();
            row.addData("DUMP KEYS");
            row.addData("Dump all the keys currently in the cache");
            rs.addRow(row); 
            
            row = new MySQL_Row();
            row.addData("EHCACHE HELP");
            row.addData("This help");
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("FLUSHALL");
            row.addData("Removes all cached items");
            rs.addRow(row); 
            
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
        Plugin_Ehcache.cache.releaseWriteLockOnKey(this.key);
    }
}
