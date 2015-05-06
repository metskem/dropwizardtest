package nl.computerhok;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

public class HelloWorldTask extends Task{
    private static Logger LOG = LoggerFactory.getLogger(HelloWorldTask.class.getName());
    protected HelloWorldTask() {
        super("helloworld-task");
    }

    @Override
    public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter printWriter) throws Exception {
        String msg = "task executed :-)";
        LOG.error(msg);
        printWriter.write(msg+"\n");
    }
}
