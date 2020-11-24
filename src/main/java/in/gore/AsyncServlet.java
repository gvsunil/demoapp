package in.gore;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns={"/async"}, asyncSupported=true)
public class AsyncServlet extends HttpServlet {


    private static Logger logger =Logger.getLogger(AsyncServlet.class.getName());

    private static Executor executor = new ThreadPoolExecutor(10,10,2000,
            TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(20));





    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        boolean doAsync = Boolean.valueOf(request.getParameter("doAsync"));

        logger.info("Starting servlet processing");
        if (doAsync) {
            final AsyncContext ac = request.startAsync(request, response);
            executor.execute(() -> {
                try {
                    Thread.sleep(5000);
                    final PrintWriter writer = new PrintWriter(response.getWriter());
                    writer.println("Async response : "+ Thread.currentThread().getId());
                    writer.flush();
                } catch (Exception exp) {

                }
                logger.info("async request handled");
                ac.complete();
            });

        } else {
            logger.info("handling sync request");
            final PrintWriter printWriter = new PrintWriter(response.getWriter());
            printWriter.println("Sync Response");
            printWriter.flush();
        }

        logger.info("exiting doGet");

    }
}
