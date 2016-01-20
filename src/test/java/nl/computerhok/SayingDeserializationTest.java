package nl.computerhok;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.junit.Assert.assertEquals;


public class SayingDeserializationTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void deserializesFromJSON() throws Exception {
        final Saying expected = new Saying(1, "Hello, dag knul!");
        String result = MAPPER.readValue(fixture("fixtures/saying.json"), Saying.class).toString();
        assertEquals("incorrect json after deserialisation", result, expected.toString());
    }
}
