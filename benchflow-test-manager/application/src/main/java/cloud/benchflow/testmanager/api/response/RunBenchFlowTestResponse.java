package cloud.benchflow.testmanager.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 18.12.16.
 */
public class RunBenchFlowTestResponse {

  @NotEmpty
  @JsonProperty
  private String testID;
  @NotEmpty
  @JsonProperty
  private String status;

  public RunBenchFlowTestResponse() {}

  public RunBenchFlowTestResponse(String testID, String status) {
    this.testID = testID;
    this.status = status;
  }

  public String getTestID() {
    return testID;
  }

  public void setTestID(String testID) {
    this.testID = testID;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
