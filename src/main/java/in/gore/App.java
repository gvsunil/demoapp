package in.gore;

import in.gore.jmx.impl.CacheCounterMBeanImpl;
import in.gore.jmx.impl.SingletonMBeanImpl;
import in.gore.resources.DetailsResource;
import in.gore.resources.QuoteResource;
import in.gore.resources.Transactions;
import in.gore.util.ValueGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Random;
import javax.management.MBeanServer;
import javax.management.ObjectName;


public class App extends Application<TestConfiguration> {
	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	@Override
	public void initialize(Bootstrap<TestConfiguration> b) {

	}

	@Override
	public void run(TestConfiguration c, Environment e) throws Exception {
		LOGGER.info("Method App#run called.");
		e.jersey().register(new QuoteResource());
		e.jersey().register(new DetailsResource());
		e.jersey().register(new Transactions());

		e.getApplicationContext().addServlet(AsyncServlet.class, "/async");

		// register MBean with jmx server.
        // refer to http://www.oracle.com/us/technologies/java/b./est-practices-jsp-136021.html
        // for details on naming conventions.
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		mbs.registerMBean(new CacheCounterMBeanImpl(), new ObjectName("in.gore:type=CacheCounter,name=Test1"));
        mbs.registerMBean(new CacheCounterMBeanImpl(), new ObjectName("in.gore:type=CacheCounter,name=Test2"));
		mbs.registerMBean(new CacheCounterMBeanImpl(), new ObjectName("in.gore:type=CacheCounter2,name=Test1"));
		mbs.registerMBean(new CacheCounterMBeanImpl(), new ObjectName("in.gore:type=CacheCounter2,name=Test2"));
		mbs.registerMBean(new CacheCounterMBeanImpl(), new ObjectName("in.gore:ServerRuntime=server1,type=CacheCounter,name=TPCount"));
		mbs.registerMBean(new CacheCounterMBeanImpl(), new ObjectName("in.gore:ServerRuntime=server2,type=CacheCounter,name=TPCount"));

		mbs.registerMBean(new SingletonMBeanImpl(), new ObjectName("in.gore:type=ClassesLoaded"));

        LOGGER.info("MBeans registered");

        try {
        	Class<?> agent = Class.forName("com.appdynamics.apm.appagent.api.AgentDelegate");
			LOGGER.info("Agent Found");
			final Object publisher = agent.getMethod("getMetricAndEventPublisher").invoke(null);
			Class<?> publisherCls = publisher.getClass();
			final Method sumMetricReporter = publisherCls.getMethod(
					"reportSumMetric", new Class[] {String.class, long.class});

			final Method reportMetric = publisherCls.getMethod(
					"reportMetric", new Class[] {String.class, long.class, String.class, String.class, String.class});

			Thread customMetric = new Thread(new Runnable() {

				@Override
				public void run() {
					Random r = r = new Random();
					ValueGenerator v = new ValueGenerator(5, 13, 1);
					String tierName = System.getProperty("appdynamics.agent.tierName");
					String metricName = "Server|Component:" +tierName + "|Custom Metrics|PCF Firehose Monitor|System (BOSH) Metrics|bosh-system-metrics-forwarder|bug1|mysql|bug2|bosh-system-metrics-forwarder.system.mem.percent";
					while (!Thread.interrupted()) {
						try {
							Thread.sleep(60 *1000);
							// generate a random value between 10 and 100
							int value = r.nextInt(100 - 10) + 10;
							LOGGER.info("Next value is " + value);
							// this registers a tier specific custom metric
							reportMetric.invoke(publisher, metricName, r.nextInt(3), "SUM", "AVERAGE", "COLLECTIVE");
							sumMetricReporter.invoke(publisher, "p1|p2|val",value);
							//sumMetricReporter.invoke(publisher, "PathParam1|PathParam11|PathParam3|PathParam5|MetricName_g",r.nextInt(3));
							//sumMetricReporter.invoke(publisher, "AWS|Internal|ELB|cloudwatch.monitoring|eu-west-1|LoadBalancer Name|awseb-e-c-AWSEBLoa-1P15LPC514C8G|Availability Zone|eu-west-1b|UnHealthyHostCount",r.nextInt(3));
							//sumMetricReporter.invoke(publisher, "AWS|Internal|ELB|cloudwatch.monitoring|eu-west-1|LoadBalancer Name|awseb-e-c-AWSEBLoa-1P15LPC514C8G|Availability Zone|eu-west-1a|Latency",r.nextInt(3));
							//sumMetricReporter.invoke(publisher, "AWS|Internal|ELB|cloudwatch.monitoring|eu-west-1|LoadBalancer Name|userapi-elb|Availability Zone|Mihir|Latency",r.nextInt(3));

							//sumMetricReporter.invoke(publisher, "PCF Firehose Monitor|System (BOSH) Metrics|bosh-system-metrics-forwarder|mihir|mysql|gore|bosh-system-metrics-forwarder.system.mem.percent",50);


							//sumMetricReporter.invoke(publisher, "AWS|Internal|ELB|cloudwatch.monitoring|eu-west-1|LoadBalancer Name|awseb-e-c-AWSEBLoa-1P15LPC514C8G|Availability Zone|eu-west-1a|HTTPCode_Backend_4XX",r.nextInt(3));
							//sumMetricReporter.invoke(publisher, "VDS|vdsconsumer02|Conn-Pools|JNDI|pool-id-simple-279",r.nextInt(3));
							//sumMetricReporter.invoke(publisher, "AWS|Internal|ELB|cloudwatch.monitoring|eu-west-1|LoadBalancer Name|awseb-e-c-AWSEBLoa-1P15LPC514C8G|Availability Zone|eu-west-1b|Latency",r.nextInt(3));

							sumMetricReporter.invoke(publisher, "PathParam1|PathParam2|PathParam3|MetricName|3",v.getValue());
						} catch (Exception exp) {

						}
					}

				}
			});

			customMetric.start();

		} catch (Exception exp) {
        	LOGGER.info("com.appdynamics.apm.appagent.api.AgentDelegate not found");
		}

		try {
        	Thread errorThread = new Thread(new ErrorObject());
        	errorThread.start();
		} catch (Exception exp) {
        	LOGGER.info("Unable to start error thread");
		}

	}

	public static void main(String args[]) {
		try {
			new App().run(args);
		} catch (Exception exp) {
            System.out.println(exp);
		}
	}
}