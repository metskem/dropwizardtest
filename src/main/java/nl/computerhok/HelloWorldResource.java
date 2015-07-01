package nl.computerhok;

import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.caching.CacheControl;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        LOG.info("returning sayings: " + sayings);
        return sayings;
    }


    @GET
    @Timed
    @Path("{id}")
    public Saying getSaying(@PathParam("id") long id) {
        String content = dao.findContentById(id);
        return new Saying(id, content);
    }


    @GET
    @Timed
    @Path("stresstest/{factor}/{leakMemory}")
    public long stresstest(@Context HttpServletRequest request, @PathParam("factor") int factor,@PathParam("leakMemory") boolean leakMemory) {
        return StressTester.test(factor,leakMemory,request.getSession());
    }


    @GET
    @Timed
    @Path("server-info")
    public String server_info(@Context HttpServletRequest request)  {
        StringBuilder resp = new StringBuilder("<html><body><pre>");
        resp.append("\ntimestamp             : " + new LocalDateTime());
        resp.append("\nrequest uri           : " + request.getRequestURI());
        resp.append("\nctx version (maj/min) : " + request.getServletContext().getMajorVersion() + "/" + request.getServletContext().getMinorVersion());
        resp.append("\ncontext server info   : " + request.getServletContext().getServerInfo());

        resp.append("\ncurrent session       : " + request.getSession(false));

        resp.append("\nremote user           : " + request.getRemoteUser());
        resp.append("\nremote host           : " + request.getRemoteHost());
        try {
            resp.append("\nresponding host : " + InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // dump envvars
        Map envMap = System.getenv();
        resp.append("\n  ----   env vars -----");
        Set envKeys = envMap.keySet();
        for (Object envKey :envKeys) {
            resp.append("\n" + envKey + ":" + envMap.get(envKey));
        }

        resp.append("\n  ----------");
        resp.append("</pre></body></html>");
        return resp.toString();
    }


    @DELETE
    @Timed
    @Path("{id}")
    public Response delete(@PathParam("id") long id) {
        dao.delete(id);
        LOG.error("deleted content with id " + id);
        return Response.ok().build();
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