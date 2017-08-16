package cloud.benchflow.experimentmanager.configurations.factory;

import cloud.benchflow.experimentmanager.services.external.MinioService;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 26/11/15.
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

  public MinioService build() throws InvalidPortException, InvalidEndpointException {

    MinioClient minioClient = new MinioClient(getAddress(), getAccessKey(), getSecretKey());

    return new MinioService(minioClient);
  }
}
