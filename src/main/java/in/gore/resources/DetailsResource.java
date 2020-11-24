package in.gore.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by mihirgore on 14/02/17.
 */
@Path("/details")
public class DetailsResource {

    private static Logger logger =Logger.getLogger(DetailsResource.class.getName());

    private Executor executor;

    public DetailsResource() {
        executor = new ThreadPoolExecutor(1,10,2000,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(20));
    }

    private static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception exp) {

        }
    }

    @GET
    public void asyncGet(@Suspended final AsyncResponse asyncResponse) throws Exception {
        logger.info("Request received");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("Response thread started :"+ Thread.currentThread().getId());
                DetailsResource.sleep(5000);
                asyncResponse.resume("{done: true}");
            }
        });
    }


}
