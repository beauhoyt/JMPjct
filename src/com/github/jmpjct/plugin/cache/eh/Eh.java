package com.github.jmpjct.plugin.cache.eh;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.terracotta.TerracottaNotRunningException;
import com.github.jmpjct.JMP;
import com.github.jmpjct.plugin.Base;
import com.github.jmpjct.Engine;
import com.github.jmpjct.mysql.proto.MySQL_Flags;
import com.github.jmpjct.mysql.proto.MySQL_ERR;
import com.github.jmpjct.mysql.proto.MySQL_OK;
import com.github.jmpjct.mysql.proto.MySQL_ResultSet_Text;
import com.github.jmpjct.mysql.proto.MySQL_Column;
import com.github.jmpjct.mysql.proto.MySQL_Row;

public class Eh extends Base {
    private static Ehcache cache = null;
    private static CacheManager cachemanager = null;
    
    public Logger logger = Logger.getLogger("Plugin.Ehcache");
    private int TTL = 0;
    private String key = "";
    
    public Eh() {
        if (Eh.cachemanager == null) {
            this.logger.trace("Eh - CacheManager: Loading "+JMP.config.getProperty("ehcacheConf"));
            Eh.cachemanager = CacheManager.create(JMP.config.getProperty("ehcacheConf").trim());
        }
        
        if (Eh.cache == null) {
            this.logger.trace("Eh - cache: Getting "+JMP.config.getProperty("ehcacheCacheName"));
            Eh.cache = Eh.cachemanager.getEhcache(JMP.config.getProperty("ehcacheCacheName").trim());
            Eh.cache.setSampledStatisticsEnabled(true);
        }
        
        if (Eh.cache == null) {
            this.logger.fatal("Eh is null! Does instance '"+JMP.config.getProperty("ehcacheCacheName")+"' exist?");
        }
    }
    
    @SuppressWarnings("unchecked")
    public void read_query(Engine context) {
        if (Eh.cache == null)
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
            
            Eh.cache.acquireWriteLockOnKey(this.key);
            
            Element element = Eh.cache.get(this.key);
            
            if (element != null) {
                this.logger.trace("Cache Hit!");
                Eh.cache.releaseWriteLockOnKey(this.key);
                
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
            
            boolean removed = Eh.cache.remove(this.key);
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
            
            Eh.cache.removeAll();
            ok.sequenceId = context.sequenceId+1;
            
            context.clear_buffer();
            context.buffer.add(ok.toPacket());
            context.nextMode = MySQL_Flags.MODE_SEND_QUERY_RESULT;
        }
        else if (command.equalsIgnoreCase("REFRESH")) {
            this.logger.trace("REFRESH");
            Eh.cache.remove(this.key);
            this.TTL = Integer.parseInt(value);
            context.buffer_result_set();
        }
        else if (command.equalsIgnoreCase("STATS")) {
            this.logger.trace("STATS");
            MySQL_ResultSet_Text rs = new MySQL_ResultSet_Text();
            MySQL_Row row = null;
            
            rs.addColumn(new MySQL_Column("Key"));
            rs.addColumn(new MySQL_Column("Value"));
            
            Statistics stats = Eh.cache.getStatistics();
            
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
            
            rs.addRow(new MySQL_Row("getGuid", Eh.cache.getGuid()));
            rs.addRow(new MySQL_Row("getName", Eh.cache.getName()));
            rs.addRow(new MySQL_Row("getStatus", Eh.cache.getStatus().toString()));
            rs.addRow(new MySQL_Row("isDisabled", Eh.cache.isDisabled()));
            rs.addRow(new MySQL_Row("isSearchable", Eh.cache.isSearchable()));
            
            try {
                rs.addRow(new MySQL_Row("isNodeBulkLoadEnabled", Eh.cache.isNodeBulkLoadEnabled()));
                rs.addRow(new MySQL_Row("isClusterBulkLoadEnabled", Eh.cache.isClusterBulkLoadEnabled()));
            }
            catch (UnsupportedOperationException e) {}
            catch (TerracottaNotRunningException e) {}
            
            rs.addRow(new MySQL_Row("isStatisticsEnabled", Eh.cache.isStatisticsEnabled()));
            rs.addRow(new MySQL_Row("isSampledStatisticsEnabled", Eh.cache.isSampledStatisticsEnabled()));
            
            switch (Eh.cache.getStatisticsAccuracy()) {
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
            
            rs.addRow(new MySQL_Row("hasAbortedSizeOf", Eh.cache.hasAbortedSizeOf()));
            
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
    
    public void read_query_result(Engine context) {
        if (Eh.cache == null)
            return;
        
        // Cache this key?
        if (this.TTL == 0 || context.buffer.size() == 0)
            return;
        
        Element element = new Element(this.key, context.buffer);
        element.setTimeToLive(this.TTL);
        Eh.cache.put(element);
        Eh.cache.releaseWriteLockOnKey(this.key);
    }
}
