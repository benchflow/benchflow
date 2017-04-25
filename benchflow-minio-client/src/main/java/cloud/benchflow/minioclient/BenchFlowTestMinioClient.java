package cloud.benchflow.minioclient;

import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public class BenchFlowTestMinioClient extends BenchFlowMinioClient {

  private static Logger logger =
      LoggerFactory.getLogger(BenchFlowTestMinioClient.class.getSimpleName());

  public BenchFlowTestMinioClient(MinioClient minioClient) {
    super(minioClient);
  }

  public void saveTestDefinition(String testID, InputStream definition) {
    logger.info("saveTestDefinition: " + testID);
    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER + PT_PE_DEFINITION_FILE_NAME;
    putInputStreamObject(definition, objectName);
  }

  public InputStream getTestDefinition(String testID) {
    logger.info("getTestDefinition: " + testID);
    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER + PT_PE_DEFINITION_FILE_NAME;
    return getInputStreamObject(objectName);
  }

  public void removeTestDefinition(String testID) {
    logger.info("removeTestDefinition: " + testID);
    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER + PT_PE_DEFINITION_FILE_NAME;
    removeObject(objectName);
  }

  public void saveTestDeploymentDescriptor(String testID, InputStream deploymentDescriptor) {
    logger.info("saveTestDeploymentDescriptor: " + testID);
    String objectName =
        minioCompatibleID(testID) + MINIO_ID_DELIMITER + DEPLOYMENT_DESCRIPTOR_FILE_NAME;
    putInputStreamObject(deploymentDescriptor, objectName);
  }

  public void copyDeploymentDescriptorForExperiment(String testID, String experimentID) {
    logger.info("copyDeploymentDescriptorForExperiment: from:" + testID + " to:" + experimentID);
    String testObjectID =
        minioCompatibleID(testID) + MINIO_ID_DELIMITER + DEPLOYMENT_DESCRIPTOR_FILE_NAME;
    String experimentObjectID =
        minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + DEPLOYMENT_DESCRIPTOR_FILE_NAME;
    copyObject(testObjectID, experimentObjectID);
  }

  public InputStream getTestDeploymentDescriptor(String testID) {
    logger.info("getTestDeploymentDescriptor: " + testID);
    String objectName =
        minioCompatibleID(testID) + MINIO_ID_DELIMITER + DEPLOYMENT_DESCRIPTOR_FILE_NAME;

    return getInputStreamObject(objectName);
  }

  public void removeTestDeploymentDescriptor(String testID) {
    logger.info("removeTestDeploymentDescriptor: " + testID);
    String objectName =
        minioCompatibleID(testID) + MINIO_ID_DELIMITER + DEPLOYMENT_DESCRIPTOR_FILE_NAME;
    removeObject(objectName);
  }

  public void saveTestBPMNModel(String testID, String modelName, InputStream model) {
    logger.info("saveTestBPMNModel: " + testID + MINIO_ID_DELIMITER + modelName);
    String objectName =
        minioCompatibleID(testID)
            + MINIO_ID_DELIMITER
            + BPMN_MODELS_FOLDER_NAME
            + MINIO_ID_DELIMITER
            + modelName;
    putInputStreamObject(model, objectName);
  }

  public void copyBPMNModelForExperiment(String testID, String experimentID, String modelName) {
    logger.info(
        "copyBPMNModelForExperiment: from:"
            + testID
            + " to:"
            + experimentID
            + " model:"
            + modelName);
    String testObjectID =
        minioCompatibleID(testID)
            + MINIO_ID_DELIMITER
            + BPMN_MODELS_FOLDER_NAME
            + MINIO_ID_DELIMITER
            + modelName;
    String experimentObjectID =
        minioCompatibleID(experimentID)
            + MINIO_ID_DELIMITER
            + BPMN_MODELS_FOLDER_NAME
            + MINIO_ID_DELIMITER
            + modelName;
    copyObject(testObjectID, experimentObjectID);
  }

  public InputStream getTestBPMNModel(String testID, String modelName) {
    logger.info("getTestBPMNModel: " + testID + MINIO_ID_DELIMITER + modelName);
    String objectName =
        minioCompatibleID(testID)
            + MINIO_ID_DELIMITER
            + BPMN_MODELS_FOLDER_NAME
            + MINIO_ID_DELIMITER
            + modelName;
    return getInputStreamObject(objectName);
  }

  public void removeTestBPMNModel(String testID, String modelName) {
    logger.info("removeTestBPMNModel: " + testID + MINIO_ID_DELIMITER + modelName);
    String objectName =
        minioCompatibleID(testID)
            + MINIO_ID_DELIMITER
            + BPMN_MODELS_FOLDER_NAME
            + MINIO_ID_DELIMITER
            + modelName;
    removeObject(objectName);
  }
}
