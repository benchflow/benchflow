package cloud.benchflow.experimentmanager.configurations.factory;

import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.ws.rs.client.Client;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 07/01/16.
 */
public class DriversMakerServiceFactory {

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

  public DriversMakerService build(Client client) {

    return new DriversMakerService(client, getAddress());
  }
}
