package cloud.benchflow.experimentmanager.configurations.factory;

import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Min;
import javax.ws.rs.client.Client;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 07/01/16.
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 */
public class DriversMakerServiceFactory {

  @NotEmpty
  private String address;

  @Min(0)
  private int numConnectionRetries;

  @JsonProperty
  public String getAddress() {
    return address;
  }

  @JsonProperty
  public void setAddress(String address) {
    this.address = address;
  }

  @JsonProperty
  public int getNumConnectionRetries() {
    return numConnectionRetries;
  }

  @JsonProperty
  public void setNumConnectionRetries(int numConnectionRetries) {
    this.numConnectionRetries = numConnectionRetries;
  }

  public DriversMakerService build(Client client) {

    return new DriversMakerService(client, getAddress(), getNumConnectionRetries());
  }
}
