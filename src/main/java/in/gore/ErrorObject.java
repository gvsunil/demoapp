package in.gore;

import org.apache.log4j.Logger;

import java.util.Random;

/**
 * Created by mihirgore on 08/06/17.
 */
public class ErrorObject implements Runnable {

    final static Logger logger = Logger.getLogger(ErrorObject.class);
    Random r = new Random();
    /**
     * Sleeps for 6 mins, then generates 4 errors.
     */
    @Override
    public void run() {

        while(!Thread.interrupted()) {
            try {
                Thread.sleep(1000*60*1);
                doErrors();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("this is an exception");
            }

        }

    }

    public int doErrors() {
        try {
            exception();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("this is an exception1");
        }
        try {
            exception();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("this is an exception2");
        }
        try {
            exception();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("this is an exception3");
        }
        try {
            exception();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("this is an exception4");
        }

        return r.nextInt(11);

    }

    public void exception() throws Exception {

        System.out.println("Throwing an Exception");
        throw new Exception("this is demo exception");

    }

}
