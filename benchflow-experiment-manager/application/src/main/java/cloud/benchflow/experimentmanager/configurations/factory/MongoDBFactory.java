package cloud.benchflow.experimentmanager.configurations.factory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.MongoClient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 05.03.17.
 */
public class MongoDBFactory {

  @NotEmpty
  private String host;
  @Min(1)
  @Max(65535)
  private int port;

  @JsonProperty("hostname")
  public String getHost() {
    return host;
  }

  @JsonProperty("hostname")
  public void setHost(String host) {
    this.host = host;
  }

  @JsonProperty
  public int getPort() {
    return port;
  }

  @JsonProperty
  public void setPort(int port) {
    this.port = port;
  }

  public MongoClient build() {

    return new MongoClient(host, port);
  }
}
