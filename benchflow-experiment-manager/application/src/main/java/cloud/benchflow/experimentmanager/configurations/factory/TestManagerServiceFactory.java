package cloud.benchflow.experimentmanager.configurations.factory;

import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.ws.rs.client.Client;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 05.03.17.
 */
public class TestManagerServiceFactory {

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

  public BenchFlowTestManagerService build(Client client) {

    return new BenchFlowTestManagerService(client, getAddress());
  }
}
