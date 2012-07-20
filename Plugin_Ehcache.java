import java.io.*;
import org.apache.log4j.Logger;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class Plugin_Ehcache extends Plugin_Base {
    public Logger logger = Logger.getLogger("Plugin.Ehcache");
    private Ehcache cache;
    
    public void init(Proxy context) {
        //this.cache = CacheManager.create();
    }
    
    public void read_query(Proxy context) {
    }
    
    public void read_query_result(Proxy context) {
    }
    
    public void send_query_result(Proxy context) {
    }
    
    public void cleanup(Proxy context) {
    }
}
