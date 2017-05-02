package cloud.benchflow.minioclient;

import io.minio.MinioClient;
import io.minio.errors.MinioException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

public class BenchFlowExperimentMinioClient extends BenchFlowMinioClient {

  private static Logger logger =
      LoggerFactory.getLogger(BenchFlowExperimentMinioClient.class.getSimpleName());

  public BenchFlowExperimentMinioClient(MinioClient minioClient) {
    super(minioClient);
  }

  /** Check if there is at least one object in the storage corresponding to the given ID. */
  public boolean isValidExperimentID(String experimentID) {
    logger.info("isValidExperimentID: " + experimentID);
    String objectName =
        minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + PT_PE_DEFINITION_FILE_NAME;
    try {
      boolean valid = minioClient.bucketExists(TESTS_BUCKET);
      if (valid) {
        valid = minioClient.listObjects(TESTS_BUCKET, objectName).iterator().hasNext();
      }
      return valid;
    } catch (MinioException
        | NoSuchAlgorithmException
        | XmlPullParserException
        | IOException
        | InvalidKeyException e) {
      logger.error(e.getMessage());
      return false;
    }
  }

  public void saveExperimentDefinition(String experimentID, InputStream definitionInputStream) {
    logger.info("getExperimentDefinition: " + experimentID);
    putInputStreamObject(definitionInputStream, objectNameOfExperimentDefinition(experimentID));
  }

  public InputStream getExperimentDefinition(String experimentID) {
    logger.info("getExperimentDefinition: " + experimentID);
    return getInputStreamObject(objectNameOfExperimentDefinition(experimentID));
  }

  public void removeExperimentDefinition(String experimentID) {
    logger.info("removeExperimentDefinition: " + experimentID);
    removeObject(objectNameOfExperimentDefinition(experimentID));
  }

  public InputStream getExperimentDeploymentDescriptor(String experimentID) {
    logger.info("getExperimentDeploymentDescriptor: " + experimentID);
    return getInputStreamObject(objectNameOfDeploymentDescriptor(experimentID));
  }

  /**
   * Get content of object containing BPMN model associated with testID generated from given
   * experimentID.
   */
  public InputStream getExperimentBPMNModel(String experimentID, String modelName) {
    logger.info("getExperimentBPMNModel: " + experimentID + MINIO_ID_DELIMITER + modelName);
    String testID = testIDFromExperimentID(experimentID);
    return getInputStreamObject(objectNameOfBPMNModel(testID, modelName));
  }

  public InputStream getDriversMakerGeneratedBenchmark(String experimentID) {
    logger.info("getDriversMakerGeneratedBenchmark: " + experimentID);
    return getInputStreamObject(objectNameOfBenchmark(experimentID));
  }

  public InputStream getDriversMakerGeneratedFabanConfiguration(String trialID) {
    logger.info("getDriversMakerGeneratedFabanConfiguration: " + trialID);
    return getInputStreamObject(objectNameOfFabanConfiguration(trialID));
  }
}
