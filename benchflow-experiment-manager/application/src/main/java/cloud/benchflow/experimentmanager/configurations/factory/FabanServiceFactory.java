package cloud.benchflow.experimentmanager.configurations.factory;

import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.configurations.FabanClientConfigImpl;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 26/11/15.
 */
public class FabanServiceFactory {

  private String user;
  private String password;
  @NotEmpty
  private String address;
  @NotNull
  private int submitRetries;

  @JsonProperty
  public String getUser() {
    return user;
  }

  @JsonProperty
  public void setUser(String user) {
    this.user = user;
  }

  @JsonProperty
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(String password) {
    this.password = password;
  }

  @JsonProperty
  public String getAddress() {
    return address;
  }

  @JsonProperty
  public void setAddress(String address) {
    this.address = address;
  }

  @JsonProperty
  public int getSubmitRetries() {
    return submitRetries;
  }

  @JsonProperty
  public void setSubmitRetries(int submitRetries) {
    this.submitRetries = submitRetries;
  }

  public FabanClient build() throws URISyntaxException {

    // do not pass null to FabanClient
    String userName = user == null ? "" : user;
    String pass = password == null ? "" : password;

    return new FabanClient()
        .withConfig(new FabanClientConfigImpl(userName, pass, new URI(getAddress())));
  }
}
