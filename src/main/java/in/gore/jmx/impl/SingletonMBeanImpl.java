package in.gore.jmx.impl;

import in.gore.jmx.api.SingletonMXBean;

import java.util.Random;

/**
 * Created by mihirgore on 16/12/16.
 */
public class SingletonMBeanImpl implements SingletonMXBean {

    private Random r;

    public SingletonMBeanImpl() {
        r = new Random();
    }

    @Override
    public int getClassesLoadedCount() {
        return r.nextInt(151);
    }
}
