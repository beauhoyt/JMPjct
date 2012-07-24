import java.util.*;
import org.apache.log4j.Logger;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.terracotta.TerracottaNotRunningException;

public class Plugin_Ehcache extends Plugin_Base {
    private static Ehcache cache = null;
    private static CacheManager cachemanager = null;
    
    public Logger logger = Logger.getLogger("Plugin.Ehcache");
    private int TTL = 0;
    private String key = "";
    
    public Plugin_Ehcache() {
        if (Plugin_Ehcache.cachemanager == null) {
            this.logger.trace("Ehcache - CacheManager: Loading "+System.getProperty("ehcacheConf"));
            Plugin_Ehcache.cachemanager = CacheManager.create(System.getProperty("ehcacheConf"));
        }
        
        if (Plugin_Ehcache.cache == null) {
            this.logger.trace("Ehcache - cache: Getting "+System.getProperty("ehcacheCacheName"));
            Plugin_Ehcache.cache = Plugin_Ehcache.cachemanager.getEhcache(System.getProperty("ehcacheCacheName"));
            Plugin_Ehcache.cache.setSampledStatisticsEnabled(true);
        }
        
        if (Plugin_Ehcache.cache == null) {
            this.logger.fatal("Ehcache is null! Does instance '"+System.getProperty("ehcacheCacheName")+"' exist?");
        }
    }
    
    @SuppressWarnings("unchecked")
    public void read_query(Proxy context) {
        if (Plugin_Ehcache.cache == null)
            return;
        
        String query = context.query;
        String command = "";
        String value = "";
        
        // Reset all values on a new query
        this.TTL = 0;
        this.key = "";
        
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
            context.buffer_result_set();
            
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
            context.buffer_result_set();
        }
        else if (command.equalsIgnoreCase("STATS")) {
            this.logger.trace("STATS");
            MySQL_ResultSet_Text rs = new MySQL_ResultSet_Text();
            MySQL_Row row = null;
            MySQL_Column key = new MySQL_Column("Key");
            rs.addColumn(key);
            MySQL_Column val = new MySQL_Column("Value");
            rs.addColumn(val);
            
            Statistics stats = Plugin_Ehcache.cache.getStatistics();
            
            row = new MySQL_Row();
            row.addData("AverageGetTime");
            row.addData(stats.getAverageGetTime());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("AverageSearchTime");
            row.addData(stats.getAverageSearchTime() );
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("CacheHits");
            row.addData(stats.getCacheHits());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("CacheMisses");
            row.addData(stats.getCacheMisses());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("DiskStoreObjectCount");
            row.addData(stats.getDiskStoreObjectCount());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("EvictionCount");
            row.addData(stats.getEvictionCount());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("InMemoryHits");
            row.addData(stats.getInMemoryHits());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("InMemoryMisses");
            row.addData(stats.getInMemoryMisses());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("MemoryStoreObjectCount");
            row.addData(stats.getMemoryStoreObjectCount());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("ObjectCount");
            row.addData(stats.getObjectCount());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("OffHeapHits");
            row.addData(stats.getOffHeapHits());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("OffHeapMisses");
            row.addData(stats.getOffHeapMisses());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("OffHeapStoreObjectCount");
            row.addData(stats.getOffHeapStoreObjectCount());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("OnDiskHits");
            row.addData(stats.getOnDiskHits());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("OnDiskMisses");
            row.addData(stats.getOnDiskMisses());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("SearchesPerSecond");
            row.addData(stats.getSearchesPerSecond());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("WriterQueueSize");
            row.addData(stats.getWriterQueueSize());
            rs.addRow(row);
            
            context.clear_buffer();
            context.buffer = rs.toPackets();
            context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
        }
        else if (command.equalsIgnoreCase("INFO")) {
            this.logger.trace("INFO");
            MySQL_ResultSet_Text rs = new MySQL_ResultSet_Text();
            MySQL_Row row = null;
            MySQL_Column key = new MySQL_Column("Key");
            rs.addColumn(key);
            MySQL_Column val = new MySQL_Column("Value");
            rs.addColumn(val);
            
            row = new MySQL_Row();
            row.addData("getGuid");
            row.addData(Plugin_Ehcache.cache.getGuid());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("getName");
            row.addData(Plugin_Ehcache.cache.getName());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("getStatus");
            row.addData(Plugin_Ehcache.cache.getStatus().toString());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("isDisabled");
            row.addData(Plugin_Ehcache.cache.isDisabled());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("isSearchable");
            row.addData(Plugin_Ehcache.cache.isSearchable());
            rs.addRow(row);
            
            try {
                row = new MySQL_Row();
                row.addData("isNodeBulkLoadEnabled");
                row.addData(Plugin_Ehcache.cache.isNodeBulkLoadEnabled());
                rs.addRow(row);
                
                row = new MySQL_Row();
                row.addData("isClusterBulkLoadEnabled");
                row.addData(Plugin_Ehcache.cache.isClusterBulkLoadEnabled());
                rs.addRow(row);
            }
            catch (UnsupportedOperationException e) {}
            catch (TerracottaNotRunningException e) {}
            
            row = new MySQL_Row();
            row.addData("isStatisticsEnabled");
            row.addData(Plugin_Ehcache.cache.isStatisticsEnabled());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("isSampledStatisticsEnabled");
            row.addData(Plugin_Ehcache.cache.isSampledStatisticsEnabled());
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("getStatisticsAccuracy");
            switch (Plugin_Ehcache.cache.getStatisticsAccuracy()) {
                case Statistics.STATISTICS_ACCURACY_BEST_EFFORT:
                    row.addData("STATISTICS_ACCURACY_BEST_EFFORT");
                    break;
                case Statistics.STATISTICS_ACCURACY_GUARANTEED:
                    row.addData("STATISTICS_ACCURACY_GUARANTEED");
                    break;
                case Statistics.STATISTICS_ACCURACY_NONE:
                    row.addData("STATISTICS_ACCURACY_NONE");
                    break;
                default:
                    row.addData("STATISTICS_ACCURACY_UNKNOWN");
                    break;
            }
            rs.addRow(row);
            
            row = new MySQL_Row();
            row.addData("hasAbortedSizeOf");
            row.addData(Plugin_Ehcache.cache.hasAbortedSizeOf());
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
        if (Plugin_Ehcache.cache == null)
            return;
        
        // Cache this key?
        if (this.TTL == 0 || context.buffer.size() == 0)
            return;
        
        Element element = new Element(this.key, context.buffer);
        element.setTimeToLive(this.TTL);
        Plugin_Ehcache.cache.put(element);
        Plugin_Ehcache.cache.releaseWriteLockOnKey(this.key);
    }
}
