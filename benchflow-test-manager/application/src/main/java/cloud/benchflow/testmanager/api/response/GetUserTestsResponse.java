package cloud.benchflow.testmanager.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-09-18
 */
public class GetUserTestsResponse {

  @NotNull
  @JsonProperty
  private List<String> testIDs = new ArrayList<>();

  public List<String> getTestIDs() {
    return testIDs;
  }

  public void setTestIDs(List<String> testIDs) {
    this.testIDs = testIDs;
  }
}
