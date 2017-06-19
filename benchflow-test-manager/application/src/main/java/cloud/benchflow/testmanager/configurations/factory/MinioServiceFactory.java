package cloud.benchflow.testmanager.configurations.factory;

import cloud.benchflow.testmanager.services.external.MinioService;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 13.02.17.
 */
public class MinioServiceFactory {

  @NotEmpty
  private String address;
  @NotEmpty
  private String accessKey;
  @NotEmpty
  private String secretKey;

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

  /**
   * Build minio service client.
   *
   * @return MinioService
   * @throws InvalidPortException if port is not open
   * @throws InvalidEndpointException if endpoint does not exist
   */
  public MinioService build() throws InvalidPortException, InvalidEndpointException {

    MinioClient minioClient = new MinioClient(getAddress(), getAccessKey(), getSecretKey());

    return new MinioService(minioClient);
  }
}
