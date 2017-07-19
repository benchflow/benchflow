package cloud.benchflow.datamanager.service.configurations;

import cloud.benchflow.datamanager.service.configurations.factory.CassandraServiceFactory;
import cloud.benchflow.datamanager.service.configurations.factory.GoogleDriveServiceFactory;
import cloud.benchflow.datamanager.service.configurations.factory.MinioServiceFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DataManagerConfiguration extends Configuration {

  // Minio Service
  @Valid
  @NotNull
  @JsonProperty
  private MinioServiceFactory minioServiceFactory = new MinioServiceFactory();

  // GoogleDrive Service
  @Valid
  @NotNull
  @JsonProperty
  private GoogleDriveServiceFactory googleDriveServiceFactory = new GoogleDriveServiceFactory();

  // Cassandra Service
  @Valid
  @NotNull
  @JsonProperty
  private CassandraServiceFactory cassandraServiceFactory = new CassandraServiceFactory();

  // Swagger Configuration
  @Valid
  @NotNull
  private final SwaggerBundleConfiguration swagger = new SwaggerBundleConfiguration();

  @JsonProperty("minio")
  public MinioServiceFactory getMinioServiceFactory() {
    return minioServiceFactory;
  }

  @JsonProperty("minio")
  public void setMinioServiceFactory(MinioServiceFactory minioServiceFactory) {
    this.minioServiceFactory = minioServiceFactory;
  }

  @JsonProperty("googleDrive")
  public GoogleDriveServiceFactory getGoogleDriveServiceFactory() {
    return googleDriveServiceFactory;
  }

  @JsonProperty("googleDrive")
  public void setGoogleDriveServiceFactory(GoogleDriveServiceFactory googleDriveServiceFactory) {
    this.googleDriveServiceFactory = googleDriveServiceFactory;
  }

  @JsonProperty("cassandra")
  public CassandraServiceFactory getCassandraServiceFactory() {
    return cassandraServiceFactory;
  }

  @JsonProperty("cassandra")
  public void setCassandraServiceFactory(CassandraServiceFactory cassandraServiceFactory) {
    this.cassandraServiceFactory = cassandraServiceFactory;
  }

  @JsonProperty("swagger")
  public SwaggerBundleConfiguration getSwagger() {
    return swagger;
  }

}
