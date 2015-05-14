package nl.computerhok;

import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.caching.CacheControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Path("/helloworld")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    private static Logger LOG = LoggerFactory.getLogger(HelloWorldResource.class);
    private final String template;
    private final String defaultName;
    private final AtomicLong counter;
    private final String RESOURCE_PATH = "helloworld";
    private final SayingDAO dao;

    public HelloWorldResource(SayingDAO dao, String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
        this.dao = dao;
    }

    @GET
    @Timed
    @CacheControl(maxAge = 6, maxAgeUnit = TimeUnit.HOURS)
    public List<Saying> findAll(@QueryParam("name") Optional<String> name) {
        List<Saying> sayings = dao.findAll();
        LOG.error("returning sayings: " + sayings);
        return sayings;
    }


    @GET
    @Timed
    @Path("{id}")
    public Saying getSaying(@PathParam("id") long id) {
        String content = dao.findContentById(id);
        return new Saying(id, content);
    }

    @POST
    public Response create(@Auth String user, Saying saying) throws Exception {
        try {
            if (saying.getContent().contains("exception")) {
                throw new IllegalArgumentException("dag knul, je wou een exception, hier heb je m");
            }
            long newid = dao.insert(saying.getContent());
            saying.setId(newid);
            LOG.error("user " + user + " created saying " + saying);
            return Response.created(new URI(RESOURCE_PATH + "/" + newid)).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}