package nl.computerhok;

import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.caching.CacheControl;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private static int simpleHitCounter;
    private static final LocalDateTime startTime = new LocalDateTime();

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
    public long stresstest(@Context HttpServletRequest request, @PathParam("factor") int factor, @PathParam("leakMemory") boolean leakMemory) {
        return StressTester.test(factor, leakMemory, request.getSession());
    }


    @GET
    @Timed
    @Path("/server-info")
    @Produces(MediaType.TEXT_HTML)
    public String server_info(@Context HttpServletRequest request) {
        StringBuilder payload = new StringBuilder();
//        payload.append("<html><body><table border=1 bgcolor=\"FF8C00\"> ");   // orange background
        payload.append("<html><body><table border=1 bgcolor=\"4D4DFF\"> ");   //blue background
        payload.append("<tr><td>application version</td><td>" + "1.8 </td></tr>");
        payload.append("<tr><td>server time           </td><td>" +  new LocalDateTime() + "</td></tr>");
        payload.append("<tr><td>instance start time   </td><td>" +  startTime + "</td></tr>");
        payload.append("<tr><td>instance hitcount     </td><td>" +  ++simpleHitCounter + "</td></tr>");
        payload.append("<tr><td>request uri           </td><td>" +  request.getRequestURI() + "</td></tr>");
        payload.append("<tr><td>ctx version (maj/min) </td><td>" +  request.getServletContext().getMajorVersion() + "/" + request.getServletContext().getMinorVersion() + "</td></tr>");
        payload.append("<tr><td>context server info   </td><td>" +  request.getServletContext().getServerInfo() + "</td></tr>");

        payload.append("<tr><td>current session       </td><td>" +  request.getSession(false) + "</td></tr>");

        payload.append("<tr><td>remote user           </td><td>" +  request.getRemoteUser() + "</td></tr>");
        payload.append("<tr><td>remote host           </td><td>" +  request.getRemoteHost() + "</td></tr>");
        try {
            payload.append("<tr><td>responding host       </td><td>" +  InetAddress.getLocalHost().getHostName() + "</td></tr>");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        payload.append("</table>");

        // dump envvars
        String envvarsRequestedStr = request.getParameter("envvars");
        if (envvarsRequestedStr != null) {
            Map envMap = System.getenv();
            payload.append("<br/> <table border=1> <tr> <th>envvar</th> <th>value</th> </tr>");
            Set envKeys = envMap.keySet();
            for (Object envKey : envKeys) {
                payload.append("<tr><td>" + envKey + "</td><td>" + envMap.get(envKey) + "</td></tr>");
            }
            payload.append("</table>");

        } else {
            payload.append("<br/>use the envvars query parameter to dump all environment variables");
            payload.append("</body></html>");
        }
        return payload.toString();
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