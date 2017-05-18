package cloud.benchflow.datamanager.service.configurations;

import cloud.benchflow.datamanager.service.configurations.factory.MinioServiceFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DataManagerConfiguration extends Configuration {

  // Minio Service
  @Valid
  @NotNull
  @JsonProperty
  private MinioServiceFactory minioServiceFactory = new MinioServiceFactory();

  @JsonProperty("minio")
  public MinioServiceFactory getMinioServiceFactory() {
    return minioServiceFactory;
  }

  @JsonProperty("minio")
  public void setMinioServiceFactory(MinioServiceFactory minioServiceFactory) {
    this.minioServiceFactory = minioServiceFactory;
  }

}
