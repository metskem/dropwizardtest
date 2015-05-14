package nl.computerhok;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilderSpec;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.ResourceConfig;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.ExceptionMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
    private static Logger LOG = LoggerFactory.getLogger(HelloWorldApplication.class);

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

        // support some static content (from the assets folder)
        bootstrap.addBundle(new AssetsBundle("/assets/favicon.ico", "/favicon.ico", "/favicon.ico","favicon-bundle"));
        bootstrap.addBundle(new AssetsBundle("/assets/", "/static"));
    }

    @Override
    public void run(HelloWorldConfiguration configuration, Environment environment) {

        // health check
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

        //remove the default exception mapper and a simple one (that outputs more detail)

//        removeDefaultExceptionMappers(environment);

//        environment.jersey().register(new CustomExceptionMapper());

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

    private void removeDefaultExceptionMappers(Environment environment) {
        ResourceConfig jrConfig = environment.jersey().getResourceConfig();
        Set<Object> dwSingletons = jrConfig.getSingletons();
        LOG.warn("initial set of jersey Singletons:" + dwSingletons);
        List<Object> singletonsToRemove = new ArrayList<Object>();

        for (Object singletons : dwSingletons) {
            if (singletons instanceof ExceptionMapper && !singletons.getClass().getName().contains("DropwizardResourceConfig")) {
                singletonsToRemove.add(singletons);
                LOG.error("to be removed: " + singletons);
            }
        }

        for (Object singletons : singletonsToRemove) {
            LOG.info("Deleting this ExceptionMapper: " + singletons.getClass().getName());
            jrConfig.getSingletons().remove(singletons);
        }
    }
}