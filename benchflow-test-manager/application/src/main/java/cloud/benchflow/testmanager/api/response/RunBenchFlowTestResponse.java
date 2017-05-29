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

  public RunBenchFlowTestResponse() {}

  public RunBenchFlowTestResponse(String testID) {
    this.testID = testID;
  }

  public String getTestID() {
    return testID;
  }

  public void setTestID(String testID) {
    this.testID = testID;
  }
}
