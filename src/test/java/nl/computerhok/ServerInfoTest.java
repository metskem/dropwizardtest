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

import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static org.assertj.core.api.Assertions.assertThat;

public class ServerInfoTest {
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @ClassRule
    public static final DropwizardAppRule<HelloWorldConfiguration> RULE =
            new DropwizardAppRule<>(HelloWorldApplication.class, ResourceHelpers.resourceFilePath("helloworld.yaml"));

    @Test
    public void test() throws IOException {
        final Response clientResponse = ClientBuilder.newClient().target("http://localhost:" + RULE.getLocalPort() +"/helloworld/server-info")
                .request()
                .header(ACCEPT_ENCODING, "text/html")
                .get();
        assertThat(clientResponse.getStatus() == 200);
        InputStream is = clientResponse.readEntity(InputStream.class);
//        System.out.println("response: " + IOUtils.toString(is,"UTF-8"));

    }

}