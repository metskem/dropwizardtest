package nl.computerhok;

import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldManaged implements Managed{
    private  static Logger LOG = LoggerFactory.getLogger(HelloWorldManaged.class);
    @Override
    public void start() throws Exception {
        LOG.error("invoked start() method");
    }

    @Override
    public void stop() throws Exception {
        LOG.error("invoked stop() method");
    }
}
