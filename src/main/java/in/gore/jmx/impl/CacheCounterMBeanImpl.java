package in.gore.jmx.impl;

import in.gore.jmx.api.CacheCounterMXBean;
import java.util.Random;

/**
 * Created by mihirgore on 15/12/16.
 */
public class CacheCounterMBeanImpl implements CacheCounterMXBean {

    private int cacheSize;
    private Random r;

    public CacheCounterMBeanImpl() {
        cacheSize = 10;
        r = new Random();
    }
    @Override
    public int getCacheSize() {
        // return a random value
        //return new Integer(String.valueOf(Math.random()*10));
        return r.nextInt(11);
    }

    @Override
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
}
