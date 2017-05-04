package cloud.benchflow.testmanager.configurations.factory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 13.02.17. */
public class MongoDBFactory {

  @NotEmpty private String host;

  @JsonProperty("hostname")
  public String getHost() {
    return host;
  }

  @JsonProperty("hostname")
  public void setHost(String host) {
    this.host = host;
  }

  @Min(1)
  @Max(65535)
  private int port;

  @JsonProperty
  public int getPort() {
    return port;
  }

  @JsonProperty
  public void setPort(int port) {
    this.port = port;
  }

  /** @return */
  public MongoClient build() {

    ServerAddress serverAddress = new ServerAddress(host, port);

    return new MongoClient(serverAddress);
  }
}
