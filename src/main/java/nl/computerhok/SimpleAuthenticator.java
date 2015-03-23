package nl.computerhok;

import com.google.common.base.Optional;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimpleAuthenticator implements Authenticator<BasicCredentials, String> {
    Logger LOG = LoggerFactory.getLogger(SimpleAuthenticator.class);

    @Override
    public Optional<String> authenticate(BasicCredentials credentials) throws AuthenticationException {
        LOG.error("authenticating user " + credentials.getUsername());
        if ("secret".equals(credentials.getPassword())) {
            return Optional.of(credentials.getUsername());
        }
        return Optional.absent();
    }
}