package cloud.benchflow.testmanager.configurations.factory;

import cloud.benchflow.testmanager.configurations.BenchFlowTestManagerConfiguration;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.client.Client;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 13.02.17.
 */
public class ExperimentManagerServiceFactory {

    @NotEmpty
    private String address;

    @JsonProperty
    public String getAddress() {
        return address;
    }

    @JsonProperty
    public void setAddress(String address) {
        this.address = address;
    }


    /**
     * @param environment
     * @return
     */
    public BenchFlowExperimentManagerService build(BenchFlowTestManagerConfiguration config, Environment environment) {

        Client client = new JerseyClientBuilder(environment)
                .using(config.getJerseyClientConfiguration())
                .build(environment.getName());

        return new BenchFlowExperimentManagerService(client, getAddress());

    }
}
