package cloud.benchflow.experimentmanager.configurations.factory;

import cloud.benchflow.experimentmanager.services.external.faban.FabanManagerService;
import cloud.benchflow.faban.client.FabanClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Min;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-08-18
 */
public class FabanManagerServiceFactory {

  @Min(0)
  private int numConnectionRetries;

  @JsonProperty
  public int getNumConnectionRetries() {
    return numConnectionRetries;
  }

  @JsonProperty
  public void setNumConnectionRetries(int numConnectionRetries) {
    this.numConnectionRetries = numConnectionRetries;
  }

  public FabanManagerService build(FabanClient fabanClient) {

    return new FabanManagerService(fabanClient, numConnectionRetries);

  }

}
