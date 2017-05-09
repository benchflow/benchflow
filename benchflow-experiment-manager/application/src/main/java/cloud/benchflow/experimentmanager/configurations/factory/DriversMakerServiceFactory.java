package cloud.benchflow.experimentmanager.configurations.factory;

import cloud.benchflow.experimentmanager.configurations.BenchFlowExperimentManagerConfiguration;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.client.Client;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *     <p>Created on 07/01/16.
 */
public class DriversMakerServiceFactory {

  @NotEmpty private String address;

  @JsonProperty
  public String getAddress() {
    return address;
  }

  @JsonProperty
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * @param config
   * @param environment
   * @return
   */
  public DriversMakerService build(Client client) {

    return new DriversMakerService(client, getAddress());
  }
}
