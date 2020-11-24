package in.gore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.BufferPoolMXBean;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadTester {

    private static final int MAX_THREADS = 100;

    public static void main(String []args)  throws Exception{
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update("Fri, 29 Sep 2017 14:06:56 GMT".getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);
        ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 0; i < MAX_THREADS; i++) {
            executorService.submit(new UrlReaderTask("http://localhost:9095/async?doAsync=true"));
        }
    }

    public static class UrlReaderTask implements Runnable {

        private String url;

        public UrlReaderTask(String url) {
            this.url = url;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(MessageFormat.format("thread[{0}] : data[{1}]", Thread.currentThread().getId(), inputLine));
                }
            } catch (Exception exp) {

            }
        }
    }
}
