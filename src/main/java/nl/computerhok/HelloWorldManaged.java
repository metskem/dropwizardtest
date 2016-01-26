package nl.computerhok;

import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldManaged implements Managed{
    private  static Logger LOG = LoggerFactory.getLogger(HelloWorldManaged.class);
    private HelloWorldConfiguration configuration;

    public HelloWorldManaged(HelloWorldConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start() throws Exception {
        LOG.error("invoked start() method");
    }

    @Override
    public void stop() throws Exception {
        LOG.error("invoked stop() method");
        ConsulServiceRegistry consulServiceRegistry = new ConsulServiceRegistry(configuration);
        consulServiceRegistry.deregisterService();
    }
}
