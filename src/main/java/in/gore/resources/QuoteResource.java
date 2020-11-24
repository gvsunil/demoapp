package in.gore.resources;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by mihirgore on 21/06/16.
 */
@Path("/quotes")
public class QuoteResource {

    public static class MyException extends Exception {
        public MyException(String str) {
            super(str);
        }
    }

    @GET
    public String newQuote() throws Exception {

//        if (Math.random() < 0.5)
//            throw new MyException("My Exception");

        return "Life is like riding a bicycle. You have to keep moving to keep your balance";
    }

    @Path("/yahoo")
    @GET
    public String request() throws Exception {
        URL yahoo = new URL("http://www.yahoo.com/");
        URLConnection yc = yahoo.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        StringBuffer buffer = new StringBuffer();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            buffer.append(inputLine);
        }
        in.close();
        return buffer.toString();
    }
}
