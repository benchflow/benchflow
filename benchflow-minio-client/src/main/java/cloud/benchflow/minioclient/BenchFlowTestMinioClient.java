package cloud.benchflow.minioclient;

import io.minio.MinioClient;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchFlowTestMinioClient extends BenchFlowMinioClient {

  private static Logger logger =
      LoggerFactory.getLogger(BenchFlowTestMinioClient.class.getSimpleName());

  public BenchFlowTestMinioClient(MinioClient minioClient) {
    super(minioClient);
  }

  public void saveTestDefinition(String testID, InputStream definition) {
    logger.info("saveTestDefinition: " + testID);
    putInputStreamObject(definition, objectNameOfTestDefinition(testID));
  }

  public InputStream getTestDefinition(String testID) {
    logger.info("getTestDefinition: " + testID);
    return getInputStreamObject(objectNameOfTestDefinition(testID));
  }

  public void removeTestDefinition(String testID) {
    logger.info("removeTestDefinition: " + testID);
    removeObject(objectNameOfTestDefinition(testID));
  }

  public void saveTestDeploymentDescriptor(String testID, InputStream deploymentDescriptor) {
    logger.info("saveTestDeploymentDescriptor: " + testID);
    putInputStreamObject(deploymentDescriptor, objectNameOfDeploymentDescriptor(testID));
  }

  /**
   * Create an object containing a experiment deployment descriptor by copying the content from the
   * object containing a test deployment descriptor.
   */
  public void copyDeploymentDescriptorForExperiment(String testID, String experimentID) {
    logger.info("copyDeploymentDescriptorForExperiment: from:" + testID + " to:" + experimentID);
    String testObjectID = objectNameOfDeploymentDescriptor(testID);
    String experimentObjectID = objectNameOfDeploymentDescriptor(experimentID);
    copyObject(testObjectID, experimentObjectID);
  }

  public InputStream getTestDeploymentDescriptor(String testID) {
    logger.info("getTestDeploymentDescriptor: " + testID);
    return getInputStreamObject(objectNameOfDeploymentDescriptor(testID));
  }

  public void removeTestDeploymentDescriptor(String testID) {
    logger.info("removeTestDeploymentDescriptor: " + testID);
    removeObject(objectNameOfDeploymentDescriptor(testID));
  }

  public void saveTestBPMNModel(String testID, String modelName, InputStream model) {
    logger.info("saveTestBPMNModel: " + testID + MINIO_ID_DELIMITER + modelName);
    putInputStreamObject(model, objectNameOfBPMNModel(testID, modelName));
  }

  public void copyBPMNModelForExperiment(String testID, String experimentID, String modelName) {
    logger.info(
        "copyBPMNModelForExperiment: from:"
            + testID
            + " to:"
            + experimentID
            + " model:"
            + modelName);
    String testObjectID = objectNameOfBPMNModel(testID, modelName);
    String experimentObjectID = objectNameOfBPMNModel(experimentID, modelName);
    copyObject(testObjectID, experimentObjectID);
  }

  public InputStream getTestBPMNModel(String testID, String modelName) {
    logger.info("getTestBPMNModel: " + testID + MINIO_ID_DELIMITER + modelName);
    return getInputStreamObject(objectNameOfBPMNModel(testID, modelName));
  }

  public void removeTestBPMNModel(String testID, String modelName) {
    logger.info("removeTestBPMNModel: " + testID + MINIO_ID_DELIMITER + modelName);
    removeObject(objectNameOfBPMNModel(testID, modelName));
  }
}
