package cloud.benchflow.datamanager.service.configurations.factory;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import cloud.benchflow.datamanager.core.datarepository.cassandra.Cassandra;
import cloud.benchflow.datamanager.core.datarepository.cassandra.CassandraImpl;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;


public class CassandraServiceFactory {

  @NotEmpty
  private String host;

  @NotEmpty
  private String keyspace;

  private String username;

  private String password;

  @JsonProperty
  public String getHost() {
    return host;
  }

  @JsonProperty
  public void setHost(String host) {
    this.host = host;
  }

  @JsonProperty
  public String getKeyspace() {
    return keyspace;
  }

  @JsonProperty
  public void setKeyspace(String keyspace) {
    this.keyspace = keyspace;
  }

  @JsonProperty
  public String getUsername() {
    return username;
  }

  @JsonProperty
  public void setUsername(String username) {
    this.username = username;
  }

  @JsonProperty
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(String password) {
    this.password = password;
  }


  public Cassandra build(ActorSystem system, Materializer materializer) {
    return new CassandraImpl(getHost(), getUsername(), getPassword(), getKeyspace(), system,
        materializer);
  }
}
