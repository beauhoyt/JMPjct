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
        this.key = context.schema+":"+query;
        
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
            
            rs.addColumn(new MySQL_Column("Key"));
            rs.addColumn(new MySQL_Column("Value"));
            
            Statistics stats = Plugin_Ehcache.cache.getStatistics();
            
            rs.addRow(new MySQL_Row("AverageGetTime", stats.getAverageGetTime()));
            rs.addRow(new MySQL_Row("AverageSearchTime", stats.getAverageSearchTime()));
            
            rs.addRow(new MySQL_Row("ObjectCount", stats.getObjectCount()));
            rs.addRow(new MySQL_Row("MemoryStoreObjectCount", stats.getMemoryStoreObjectCount()));
            rs.addRow(new MySQL_Row("OffHeapStoreObjectCount", stats.getOffHeapStoreObjectCount()));
            rs.addRow(new MySQL_Row("DiskStoreObjectCount", stats.getDiskStoreObjectCount()));

            rs.addRow(new MySQL_Row("CacheHits", stats.getCacheHits()));
            rs.addRow(new MySQL_Row("CacheMisses", stats.getCacheMisses()));

            rs.addRow(new MySQL_Row("InMemoryHits", stats.getInMemoryHits()));
            rs.addRow(new MySQL_Row("InMemoryMisses", stats.getInMemoryMisses()));

            rs.addRow(new MySQL_Row("OffHeapHits", stats.getOffHeapHits()));
            rs.addRow(new MySQL_Row("OffHeapMisses", stats.getOffHeapMisses()));

            rs.addRow(new MySQL_Row("OnDiskHits", stats.getOnDiskHits()));
            rs.addRow(new MySQL_Row("OnDiskMisses", stats.getOnDiskMisses()));

            rs.addRow(new MySQL_Row("EvictionCount", stats.getEvictionCount()));

            rs.addRow(new MySQL_Row("SearchesPerSecond", stats.getSearchesPerSecond()));
            rs.addRow(new MySQL_Row("WriterQueueSize", stats.getWriterQueueSize()));
            
            context.clear_buffer();
            context.buffer = rs.toPackets();
            context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
        }
        else if (command.equalsIgnoreCase("INFO")) {
            this.logger.trace("INFO");
            MySQL_ResultSet_Text rs = new MySQL_ResultSet_Text();
            MySQL_Row row = null;
            
            rs.addColumn(new MySQL_Column("Key"));
            rs.addColumn(new MySQL_Column("Value"));
            
            rs.addRow(new MySQL_Row("getGuid", Plugin_Ehcache.cache.getGuid()));
            rs.addRow(new MySQL_Row("getName", Plugin_Ehcache.cache.getName()));
            rs.addRow(new MySQL_Row("getStatus", Plugin_Ehcache.cache.getStatus().toString()));
            rs.addRow(new MySQL_Row("isDisabled", Plugin_Ehcache.cache.isDisabled()));
            rs.addRow(new MySQL_Row("isSearchable", Plugin_Ehcache.cache.isSearchable()));
            
            try {
                rs.addRow(new MySQL_Row("isNodeBulkLoadEnabled", Plugin_Ehcache.cache.isNodeBulkLoadEnabled()));
                rs.addRow(new MySQL_Row("isClusterBulkLoadEnabled", Plugin_Ehcache.cache.isClusterBulkLoadEnabled()));
            }
            catch (UnsupportedOperationException e) {}
            catch (TerracottaNotRunningException e) {}
            
            rs.addRow(new MySQL_Row("isStatisticsEnabled", Plugin_Ehcache.cache.isStatisticsEnabled()));
            rs.addRow(new MySQL_Row("isSampledStatisticsEnabled", Plugin_Ehcache.cache.isSampledStatisticsEnabled()));
            
            switch (Plugin_Ehcache.cache.getStatisticsAccuracy()) {
                case Statistics.STATISTICS_ACCURACY_BEST_EFFORT:
                    rs.addRow(new MySQL_Row("getStatisticsAccuracy", "STATISTICS_ACCURACY_BEST_EFFORT"));
                    break;
                case Statistics.STATISTICS_ACCURACY_GUARANTEED:
                    rs.addRow(new MySQL_Row("getStatisticsAccuracy", "STATISTICS_ACCURACY_GUARANTEED"));
                    break;
                case Statistics.STATISTICS_ACCURACY_NONE:
                    rs.addRow(new MySQL_Row("getStatisticsAccuracy", "STATISTICS_ACCURACY_NONE"));
                    break;
                default:
                    rs.addRow(new MySQL_Row("getStatisticsAccuracy", "STATISTICS_ACCURACY_UNKNOWN"));
                    break;
            }
            
            rs.addRow(new MySQL_Row("hasAbortedSizeOf", Plugin_Ehcache.cache.hasAbortedSizeOf()));
            
            context.clear_buffer();
            context.buffer = rs.toPackets();
            context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
        }
        else if (command.equalsIgnoreCase("DUMP KEYS")) {
            this.logger.trace("DUMP KEYS");
            List keys = this.cache.getKeysWithExpiryCheck();
            
            MySQL_ResultSet_Text rs = new MySQL_ResultSet_Text();
            rs.addColumn(new MySQL_Column("Key"));
            
            for (Object k: keys) {
                this.logger.trace("Key: '"+k+"'");
                rs.addRow(new MySQL_Row(k.toString())); 
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
