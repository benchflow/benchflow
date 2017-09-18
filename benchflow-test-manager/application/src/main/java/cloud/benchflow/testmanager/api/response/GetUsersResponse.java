package cloud.benchflow.testmanager.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-09-18
 */
public class GetUsersResponse {

  @NotNull
  @JsonProperty
  private List<String> users = new ArrayList<>();

  public List<String> getUsers() {
    return users;
  }

  public void setUsers(List<String> users) {
    this.users = users;
  }
}
