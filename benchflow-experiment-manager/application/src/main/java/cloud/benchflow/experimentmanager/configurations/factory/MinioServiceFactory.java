package cloud.benchflow.experimentmanager.configurations.factory;

import cloud.benchflow.experimentmanager.services.external.MinioService;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import javax.validation.constraints.Min;
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

  @Min(1)
  private long connectTimeout;
  @Min(1)
  private long writeTimeout;
  @Min(1)
  private long readTimeout;

  @Min(0)
  private int numConnectionRetries;

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
  public long getConnectTimeout() {
    return connectTimeout;
  }

  @JsonProperty
  public void setConnectTimeout(long connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  @JsonProperty
  public long getWriteTimeout() {
    return writeTimeout;
  }

  @JsonProperty
  public void setWriteTimeout(long writeTimeout) {
    this.writeTimeout = writeTimeout;
  }

  @JsonProperty
  public long getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(long readTimeout) {
    this.readTimeout = readTimeout;
  }

  @JsonProperty
  public int getNumConnectionRetries() {
    return numConnectionRetries;
  }

  @JsonProperty
  public void setNumConnectionRetries(int numConnectionRetries) {
    this.numConnectionRetries = numConnectionRetries;
  }

  public MinioService build() throws InvalidPortException, InvalidEndpointException {

    MinioClient minioClient = new MinioClient(getAddress(), getAccessKey(), getSecretKey());

    minioClient.setTimeout(connectTimeout, writeTimeout, readTimeout);

    return new MinioService(minioClient, numConnectionRetries);
  }
}
