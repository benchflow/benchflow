package cloud.benchflow.experimentmanager.configurations.factory;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-06-04
 */
public class TestModeFactory {

  @NotNull
  private boolean mockFaban;

  @JsonProperty
  public boolean isMockFaban() {
    return mockFaban;
  }

  @JsonProperty
  public void setMockFaban(boolean mockFaban) {
    this.mockFaban = mockFaban;
  }
}
