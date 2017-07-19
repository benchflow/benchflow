package cloud.benchflow.datamanager.service.configurations.factory;

import cloud.benchflow.datamanager.core.datarepository.filestorage.ExperimentFileStorage;
import cloud.benchflow.datamanager.core.datarepository.objectstorage.MinioFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;

import org.hibernate.validator.constraints.NotEmpty;

public class MinioServiceFactory {

  @NotEmpty
  private String address;

  @NotEmpty
  private String accessKey;

  @NotEmpty
  private String secretKey;

  @NotEmpty
  private String defaultBucket;

  @JsonProperty
  public String getAddress() {
    return address;
  }

  @JsonProperty
  public void setAddress(String address) {
    this.address = address;
  }

  @JsonProperty
  public String getAccessKey() {
    return accessKey;
  }

  @JsonProperty
  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  @JsonProperty
  public String getSecretKey() {
    return secretKey;
  }

  @JsonProperty
  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  @JsonProperty
  public String getDefaultBucket() {
    return defaultBucket;
  }

  @JsonProperty
  public void setDefaultBucket(String defaultBucket) {
    this.defaultBucket = defaultBucket;
  }

  public ExperimentFileStorage build() throws InvalidPortException, InvalidEndpointException {
    MinioClient minioClient = new MinioClient(getAddress(), getAccessKey(), getSecretKey());
    return MinioFactory.apply(minioClient, getDefaultBucket());
  }
}
