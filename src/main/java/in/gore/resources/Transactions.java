package in.gore.resources;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by mihirgore on 02/08/17.
 */

@Path("/txn")
public class Transactions {

    @GET
    @Path("/slow")
    public String slow() {
        try {
            Thread.sleep(8000);
        } catch (Exception exp) {

        }

        return "done";
    }


    /**
     * Invoking this will show up as a remote service in flow map
     */
    @GET
    @Path("/remote")
    public String remote() {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet("https://www.google.com");
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader
                    (new InputStreamReader(
                            response.getEntity().getContent()));

            StringBuffer buffer = new StringBuffer();
            String line = null;
            while ((line = rd.readLine()) != null) {
                buffer.append(line);
            }

            return "Response Received";

        } catch (Exception exp) {
            return "Error Occured";
        }

    }

    /**
     * Invoking this will generate calls to another instance of demoapp running locally on port 9095
     *
     */
    @GET
    @Path("/demo/{count}")
    public String demo(@PathParam("count") int count) {
        if (count == 0) {
            return "Count reached 0";
        }
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request;
        if (count % 2 == 0) {
            request = new HttpGet("http://localhost:9095/txn/demo/" + --count);
        } else {
            request = new HttpGet("http://localhost:9090/txn/demo/" + --count);
        }
        try {
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader
                    (new InputStreamReader(
                            response.getEntity().getContent()));

            StringBuffer buffer = new StringBuffer();
            String line = null;
            while ((line = rd.readLine()) != null) {
                buffer.append(line);
            }
            return "Response received";
        } catch (IOException e) {
            return "Error Occured";
        }
    }


    @POST
    @Path("/webhook")
    public String testing(@QueryParam("token") String token) {
        return token;
    }
}
