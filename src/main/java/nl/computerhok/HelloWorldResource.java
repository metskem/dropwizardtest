package nl.computerhok;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import io.dropwizard.jersey.caching.CacheControl;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Path("/helloworld")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    public static final String VERSION = "1.4 (2016-02-12 09:19)";
    private static Logger LOG = LoggerFactory.getLogger(HelloWorldResource.class);
    private final static String RESOURCE_PATH = "helloworld";
    private final SayingDAO dao;

    private static int simpleHitCounter;
    private static final LocalDateTime startTime = new LocalDateTime();

    public HelloWorldResource(SayingDAO dao, String template, String defaultName) {
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
        payload.append("<html><body><table border=1 bgcolor=\"FF8C00\"> ");   // orange background
//        payload.append("<html><body><table border=1 bgcolor=\"4D4DFF\"> ");   //blue background
        payload.append("<tr><td>application version</td><td>" + VERSION + "</td></tr>");
        payload.append("<tr><td>server time           </td><td>" + new LocalDateTime() + "</td></tr>");
        payload.append("<tr><td>instance start time   </td><td>" + startTime + "</td></tr>");
        payload.append("<tr><td>instance hitcount     </td><td>" + ++simpleHitCounter + "</td></tr>");
        payload.append("<tr><td>request uri           </td><td>" + request.getRequestURI() + "</td></tr>");
        payload.append("<tr><td>ctx version (maj/min) </td><td>" + request.getServletContext().getMajorVersion() + "/" + request.getServletContext().getMinorVersion() + "</td></tr>");
        payload.append("<tr><td>context server info   </td><td>" + request.getServletContext().getServerInfo() + "</td></tr>");

        payload.append("<tr><td>current session       </td><td>" + request.getSession(false) + "</td></tr>");

        payload.append("<tr><td>remote user           </td><td>" + request.getRemoteUser() + "</td></tr>");
        payload.append("<tr><td>remote host           </td><td>" + request.getRemoteHost() + "</td></tr>");
        try {
            payload.append("<tr><td>responding host       </td><td>" + InetAddress.getLocalHost().getHostName() + "</td></tr>");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        payload.append("</table>");

        // dump http headers
        String headersRequested = request.getParameter("headers");
        if (headersRequested != null) {
            Enumeration<String> headerNames = request.getHeaderNames();
            payload.append("<br/> <table border=1> <tr> <th>header</th> <th>value</th> </tr>");
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                payload.append("<tr><td>" + headerName + "</td><td>" + request.getHeader(headerName) + "</td></tr>");
            }
            payload.append("</table>");

        } else {
            payload.append("<br/>use the headers query parameter to dump all headers");
        }

        // dump envvars
        String envvarsRequested = request.getParameter("envvars");
        if (envvarsRequested != null) {
            Map envMap = System.getenv();
            payload.append("<br/> <table border=1> <tr> <th>envvar</th> <th>value</th> </tr>");
            Set envKeys = envMap.keySet();
            for (Object envKey : envKeys) {
                payload.append("<tr><td>" + envKey + "</td><td>" + envMap.get(envKey) + "</td></tr>");
            }
            payload.append("</table>");

        } else {
            payload.append("<br/>use the envvars query parameter to dump all environment variables");
        }

        // delay
        String delay = request.getParameter("delay");
        if (delay != null) {
            try {
                Thread.sleep(Long.parseLong(delay));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            payload.append("<br/>use the delay query parameter to delay the response (ms)");
        }

        // query dns
        String dnsquery = request.getParameter("dnsquery");
        if (dnsquery != null) {
            String query = "_" + dnsquery + "._tcp.marathon.mesos";
            try {
                Record[] records = new Lookup(query, Type.SRV).run();
                if (records != null) {
                    payload.append("<br/> Querying " + query + "<br/> <table border=1> <tr> <th>Host/IP</th> <th>port</th> <th>ttl</th> <th>message</th> <th>SRVRecord.toString()</th></tr>");
                    for (Record record : records) {
                        SRVRecord srv = (SRVRecord) record;
                        String hostname = srv.getTarget().toString().replaceFirst("\\.$", "");
                        long ttl = srv.getTTL();
                        int port = srv.getPort();
                        String message;
                        String IP = "-";
                        String host = "-";
                        try {
                            Socket socket = new Socket(hostname,port);
                            message = "port open";
                            IP = socket.getInetAddress().getHostAddress();
                            host = socket.getInetAddress().getCanonicalHostName();
                        } catch (IOException e) {
                            message = e.getMessage();
                        }
                        payload.append("<tr><td>" + host+"/"+IP + "</td><td>" + port + "</td><td>" + ttl+ "</td><td>" + message+ "</td><td>" + srv.toString() + "</td></tr>");
                    }
                    payload.append("</table>");
                } else {
                    payload.append("<br/> dns lookup gave zero results");
                }
            } catch (TextParseException e) {
                e.printStackTrace();
            }
        } else {
            payload.append("<br/>use the dnsquery query parameter to perform a dns query, specify the marathon appname, we will query for <pre> _<appname>._tcp.marathon.mesos </pre>");
        }

        payload.append("</body></html>");

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
    public Response create(Saying saying) throws Exception {
        try {
            if (saying.getContent().contains("exception")) {
                throw new IllegalArgumentException("dag knul, je wou een exception, hier heb je m");
            }
            long newid = dao.insert(saying.getContent());
            saying.setId(newid);
            LOG.error(" created saying " + saying);
            return Response.created(new URI(RESOURCE_PATH + "/" + newid)).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}