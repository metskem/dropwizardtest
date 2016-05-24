package nl.computerhok;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

public class HelloWorldConfiguration extends Configuration {
    Logger LOG = LoggerFactory.getLogger(HelloWorldConfiguration.class);
    public final static String PROP_JDBC_VCAP_SERVICE = "JDBC_VCAP_SERVICE";

    private DataSourceFactory database = new DataSourceFactory();

    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        // check if we are at cloudfoundry and someone specified to use the VCAP_SERVICES envvar for JDBC config
        String jdbcService = System.getenv(PROP_JDBC_VCAP_SERVICE);
        if (jdbcService != null) {
            String vcap_services = System.getenv("VCAP_SERVICES");
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                LOG.debug("reading JSON config:\n" + vcap_services);
                JsonNode vcapservices = objectMapper.readTree(vcap_services);
                Iterator services = vcapservices.iterator();
                while (services.hasNext()) {
                    JsonNode service = (JsonNode) services.next();
                    Iterator iterator = service.iterator();
                    while (iterator.hasNext()) {
                        JsonNode serviceAttributes = (JsonNode) iterator.next();
                        if (serviceAttributes != null) {
                            JsonNode credentials = serviceAttributes.get("credentials");
                            if (credentials != null) {
                                JsonNode jdbcurl = credentials.get("jdbcUrl");
                                JsonNode username = credentials.get("username");
                                JsonNode password = credentials.get("password");
                                if (jdbcurl != null && username != null && password != null) {
                                    database.setUrl(jdbcurl.textValue());
                                    database.setUser(username.textValue());
                                    database.setPassword(password.textValue());
                                }
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            LOG.info("no " + PROP_JDBC_VCAP_SERVICE + " envvar found, we will not configure from envvar VCAP_SERVICES");
        }return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory database) {
        this.database = database;
    }

}

