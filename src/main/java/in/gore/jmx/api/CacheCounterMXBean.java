package in.gore.jmx.api;

/**
 * Created by mihirgore on 15/12/16.
 */
public interface CacheCounterMXBean {
    public int getCacheSize();
    public void setCacheSize(int cacheSize);
}
