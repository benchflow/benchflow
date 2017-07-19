package cloud.benchflow.datamanager.service.configurations.factory;

import cloud.benchflow.datamanager.core.backupstorage.BackupStorage;
import cloud.benchflow.datamanager.core.backupstorage.GoogleDrive;
import cloud.benchflow.datamanager.core.backupstorage.GoogleDriveImpl;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.hibernate.validator.constraints.NotEmpty;


public class GoogleDriveServiceFactory {

  @NotEmpty
  private String applicationName;

  @NotEmpty
  private String secret;

  @NotEmpty
  private String credentialsDir;

  @NotEmpty
  private String baseFolderName;

  @JsonProperty
  public String getApplicationName() {
    return applicationName;
  }

  @JsonProperty
  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  @JsonProperty
  public String getSecret() {
    return secret;
  }

  @JsonProperty
  public void setSecret(String secret) {
    this.secret = secret;
  }

  @JsonProperty
  public String getCredentialsDir() {
    return credentialsDir;
  }

  @JsonProperty
  public void setCredentialsDir(String credentialsDir) {
    this.credentialsDir = credentialsDir;
  }

  @JsonProperty
  public String getBaseFolderName() {
    return baseFolderName;
  }

  @JsonProperty
  public void setBaseFolderName(String baseFolderName) {
    this.baseFolderName = baseFolderName;
  }

  /**
   * Create a BackupStorage using a GoogleDrive instantiated using
   * information from the configuration file.
   */
  public BackupStorage build()
      throws InvalidPortException, InvalidEndpointException, IOException, GeneralSecurityException {
    GoogleDrive drive =
        new GoogleDrive(getApplicationName(), new File(getSecret()), getCredentialsDir());
    return new GoogleDriveImpl(drive, getBaseFolderName());
  }
}
