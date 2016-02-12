package nl.computerhok;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulServiceRegistry {
    private static Logger LOG = LoggerFactory.getLogger(ConsulServiceRegistry.class);
    private HelloWorldConfiguration configuration;
    private ConsulClient consulClient;

    public ConsulServiceRegistry(HelloWorldConfiguration configuration) {
        this.configuration = configuration;
    }

    public void registerService() {
        String servicename = configuration.getConsul_servicename();
        if (!"NONE".equals(servicename)) {
            if (consulClient == null) {
                consulClient = new ConsulClient();
            }
            deregisterService();

            String serviceid = configuration.getConsul_serviceid();
            int serviceport = configuration.getConsul_serviceport();
            String servicecheckhttp = configuration.getConsul_servicecheckhttp();
            String servicecheckinterval = configuration.getConsul_servicecheckinterval();
            LOG.error("registering service, servicename=" + servicename + ", serviceport=" + serviceport + ", servicecheckhttp=" + servicecheckhttp + ", servicecheckinterval=" + servicecheckinterval);

            // register new service with associated health check
            NewService newService = new NewService();
            newService.setId(serviceid);
            newService.setName(servicename);
            newService.setPort(serviceport);

            NewService.Check serviceCheck = new NewService.Check();
            serviceCheck.setHttp(servicecheckhttp);
            serviceCheck.setInterval(servicecheckinterval);
            newService.setCheck(serviceCheck);

            Response response = consulClient.agentServiceRegister(newService);
            LOG.warn("Response from agentServiceRegister(): " + response);
        } else {
            LOG.warn("envvar DW_SERVICENAME is \"NONE\" or empty, not (consul)registering service");
        }
    }

    public void deregisterService() {
        if (consulClient == null) {
            consulClient = new ConsulClient();
        }
        String servicename = configuration.getConsul_servicename();
        if (!"NONE".equals(servicename)) {
            String serviceid = configuration.getConsul_serviceid();
            LOG.error("deregistering service=" + servicename + ", serviceid=" + serviceid);
            Response response = consulClient.agentServiceDeregister(serviceid);
            LOG.warn("Response from agentServiceDeregister(): " + response);
        } else {
            LOG.warn("envvar DW_SERVICENAME is \"NONE\" or empty, not (consul)deregistering service");
        }
    }
}
