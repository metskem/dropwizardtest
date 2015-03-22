package nl.computerhok;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SayingSerializationTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void serializesToJSON() throws Exception {
        final Saying saying = new Saying(1, "Hello, dag knul!");
        assertThat(MAPPER.writeValueAsString(saying)).isEqualTo(fixture("fixtures/saying.json"));
    }
}
