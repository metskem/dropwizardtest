package nl.computerhok;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilderSpec;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName() {
        return "helloworld";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(HelloWorldConfiguration configuration, Environment environment) {

        // healthcheck
        final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
        environment.healthChecks().register("template", healthCheck);

        // managed components (do something during start and/or stop of application)
        environment.lifecycle().manage(new HelloWorldManaged());

        // tasks
        environment.admin().addTask(new HelloWorldTask());

        //  DB stuff
        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "postgresql");
        final SayingDAO dao = jdbi.onDemand(SayingDAO.class);
        final HelloWorldResource resource = new HelloWorldResource(dao, configuration.getTemplate(), configuration.getDefaultName());
        environment.jersey().register(resource);

        // authentication with cache (not used yet)
        SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator();
        MetricRegistry metricRegistry = new MetricRegistry();
        CachingAuthenticator<BasicCredentials, String> cachingAuthenticator = new CachingAuthenticator<>(metricRegistry, simpleAuthenticator, CacheBuilderSpec.parse("maximumSize=100"));

        // authentication without cache
        environment.jersey().register(AuthFactory.binder(new BasicAuthFactory<>(new SimpleAuthenticator(),"SUPER SECRET STUFF", String.class)));

//
//        environment.jersey().register(AuthFactory.binder(new BasicAuthFactory<String>(new BasicAuthFactory<CachingAuthenticator>(cachingAuthenticator)),
//                "BasicAuth Realm", User.class));
//

    }
}