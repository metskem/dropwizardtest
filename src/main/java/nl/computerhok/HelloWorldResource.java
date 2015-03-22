package nl.computerhok;

import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.caching.CacheControl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    private final String template;
    private final String defaultName;
    private final AtomicLong counter;
    private final String RESOURCE_PATH = "hello-world";

    public HelloWorldResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    @CacheControl(maxAge = 6, maxAgeUnit = TimeUnit.HOURS)
    public Saying sayHello(@QueryParam("name") Optional<String> name) {
        final String value = String.format(template, name.or(defaultName));
        return new Saying(counter.incrementAndGet(), value);
    }

    @POST
    public Response create(Saying saying) throws Exception {
        try {
            System.out.println("created " + saying);
            if (saying.getContent().contains("exception")) {
                throw new IllegalArgumentException("dag knul, je wou een exception, hier heb je m");
            }
            return Response.created(new URI(RESOURCE_PATH + "/" + "4711")).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}