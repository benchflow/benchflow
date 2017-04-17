package cloud.benchflow.datamanager.service.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Backup {
  private long id;

  public Backup() {
    // Jackson deserialization
  }

  public Backup(long id) {
    this.id = id;
  }

  @JsonProperty
  public long getId() {
    return id;
  }
}
