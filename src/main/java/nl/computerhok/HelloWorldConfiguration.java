package nl.computerhok;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import org.hibernate.validator.constraints.NotEmpty;

public class HelloWorldConfiguration extends Configuration {

    private DataSourceFactory database = new DataSourceFactory();
    
    private String consul_servicename;
    private String consul_serviceid;
    private int consul_serviceport;
    private String consul_servicecheckhttp;
    private String consul_servicecheckinterval;
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
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory database) {
        this.database = database;
    }

    public String getConsul_servicename() {
        return consul_servicename;
    }
    
    @JsonProperty
    public void setConsul_servicename(String consul_servicename) {
        this.consul_servicename = consul_servicename;
    }

    public String getConsul_serviceid() {
        return consul_serviceid;
    }

    public void setConsul_serviceid(String consul_serviceid) {
        this.consul_serviceid = consul_serviceid;
    }

    @JsonProperty
    public int getConsul_serviceport() {
        return consul_serviceport;
    }

    @JsonProperty
    public void setConsul_serviceport(int consul_serviceport) {
        this.consul_serviceport = consul_serviceport;
    }

    @JsonProperty
    public String getConsul_servicecheckinterval() {
        return consul_servicecheckinterval;
    }

    @JsonProperty
    public void setConsul_servicecheckinterval(String consul_servicecheckinterval) {
        this.consul_servicecheckinterval = consul_servicecheckinterval;
    }

    @JsonProperty
    public String getConsul_servicecheckhttp() {
        return consul_servicecheckhttp;
    }

    @JsonProperty
    public void setConsul_servicecheckhttp(String consul_servicecheckhttp) {
        this.consul_servicecheckhttp = consul_servicecheckhttp;
    }

}

