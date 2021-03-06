package nl.computerhok;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class SayingIntegrationTest {
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @ClassRule
    public static final DropwizardAppRule<HelloWorldConfiguration> RULE =
            new DropwizardAppRule<>(HelloWorldApplication.class, ResourceHelpers.resourceFilePath("helloworld.yaml"));

    @Test
    public void testGET() throws IOException {
        final Response clientResponse = ClientBuilder.newClient().target("http://localhost:" + RULE.getLocalPort() +"/helloworld/1")
                .request()
                .header(ACCEPT_ENCODING, "application/json")
                .get();
        assertThat(clientResponse.getStatus() == 200);

        String expected = MAPPER.readValue(fixture("fixtures/saying.json"), Saying.class).toString();
        Saying result = clientResponse.readEntity(Saying.class);
        assertEquals("incorrect json response body", expected, result.toString());
    }

    @Test
    public void testGET1() throws IOException {
        final Response clientResponse = ClientBuilder.newClient().target("http://localhost:" + RULE.getLocalPort() +"/helloworld/1")
                .request()
                .header(ACCEPT_ENCODING, "application/json")
                .get();
        assertThat(clientResponse.getStatus() == 200);

        String expected = MAPPER.readValue(fixture("fixtures/saying.json"), Saying.class).toString();
        Saying result = clientResponse.readEntity(Saying.class);
        assertEquals("incorrect json response body", expected, result.toString());

    }

    @Test
    public void testGETList() throws IOException {
        final Response clientResponse = ClientBuilder.newClient().target("http://localhost:" + RULE.getLocalPort() + "/helloworld")
                .request()
                .header(ACCEPT_ENCODING, "application/json")
                .get();
        assertThat(clientResponse.getStatus() == 200);

        String expected = MAPPER.readValue(fixture("fixtures/saying.json"), Saying.class).toString();
        InputStream is = clientResponse.readEntity(InputStream.class);
        System.out.println(clientResponse);
//        assertEquals("incorrect json response body", expected, result.toString());

    }
}