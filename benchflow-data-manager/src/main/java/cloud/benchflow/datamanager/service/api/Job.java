package cloud.benchflow.datamanager.service.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class Job {
  private final Long id;
  private final Optional<Long> backupId;

  public Job(Long id, Long backupId) {
    this.id = id;
    this.backupId = Optional.ofNullable(backupId);
  }

  public Job(Long id) {
    this.id = id;
    this.backupId = Optional.empty();
  }

  @JsonProperty
  public Long getId() {
    return id;
  }

  @JsonProperty
  public Optional<Long> getBackupId() {
    return backupId;
  }
}
