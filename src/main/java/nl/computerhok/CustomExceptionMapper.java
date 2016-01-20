package nl.computerhok;

import com.fasterxml.jackson.core.JsonGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class CustomExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(CustomExceptionMapper.class);
    static {
        LOG.error("static init");
    }

    @Override
    public Response toResponse(Throwable throwable) {
        LOG.error("toResponse processing exception: " + throwable);
        /*
         * If the error is in the JSON generation, it's a server error.
         */
        if (throwable instanceof JsonGenerationException) {
            LOG.warn("Error generating JSON", throwable);
            return Response.serverError().build();
        }

        final String message = throwable.getMessage();

        /*
         * If we can't deserialize the JSON because someone forgot a no-arg constructor, it's a
         * server error and we should inform the developer.
         */
        if (message.startsWith("No suitable constructor found")) {
            LOG.error("Unable to deserialize the specific type", throwable);
            return Response.serverError().build();
        }

        /*
         * Otherwise, it's a user error.
         */
        LOG.debug("Unable to process JSON", throwable);
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

}