import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Cache;
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
            this.TTL = Integer.parseInt(value);
            context.bufferResultSet = true;
            
            Element element = Plugin_Ehcache.cache.get(this.key);
            
            if (element != null) {
                this.logger.trace("Cache Hit!");
                
                context.clear_buffer();
                context.buffer = (ArrayList<byte[]>) element.getValue();
                context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
                
                if (context.buffer.size() == 0) {
                    this.logger.fatal("Buffer is empty!?!?");
                    context.halt();
                }
            }
        }
        else if (command.equals("FLUSH")) {
            MySQL_OK ok = new MySQL_OK();
            
            boolean removed = Plugin_Ehcache.cache.remove(this.key);
            if (removed)
                ok.affectedRows = 1;
            ok.sequenceId = context.sequenceId+1;
            
            context.clear_buffer();
            context.buffer.add(ok.toPacket());
            Plugin_Debug.dump_buffer(context);
            context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
        }
        else if (command.equals("REFRESH")) {
            Plugin_Ehcache.cache.remove(this.key);
            this.TTL = Integer.parseInt(value);
            context.bufferResultSet = true;
        }
        else if (command.equals("STATS")) {
            this.logger.info(Plugin_Ehcache.cache.getSize()+" Elements in Cache");
            this.logger.info(Plugin_Ehcache.cache.getMemoryStoreSize()+" Elements in Memory");
            this.logger.info(Plugin_Ehcache.cache.getDiskStoreSize()+" Elements on Disk");
            /*
            this.logger.info(Plugin_Ehcache.cache.getHitCount()+" Hits");
            this.logger.info(Plugin_Ehcache.cache.getMemoryStoreHitCount()+" Memory Hits");
            this.logger.info(Plugin_Ehcache.cache.getDiskStoreCount()+" Disk Hits");
            this.logger.info(Plugin_Ehcache.cache.getMissCountNotFound()+" Misses");
            this.logger.info(Plugin_Ehcache.cache.getMissCountExpired()+" Miss Expired");
            */
        }
        else if (command.equals("DUMP KEYS")) {
            List keys = this.cache.getKeysWithExpiryCheck();
            for( int i = 0; i < keys.size(); i++)
                this.logger.trace("Key: '"+keys.get(i)+"'");
        }
        else {
            this.logger.fatal(command+" is unknown!");
            context.halt();
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
