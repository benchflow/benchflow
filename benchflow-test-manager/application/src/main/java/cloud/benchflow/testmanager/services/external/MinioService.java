package cloud.benchflow.testmanager.services.external;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MINIO_ID_DELIMITER;
import static cloud.benchflow.testmanager.constants.BenchFlowConstants.TESTS_BUCKET;

import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;
import io.minio.messages.Item;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 18.12.16.
 */
public class MinioService {

  // http://www.iana.org/assignments/media-types/application/octet-stream
  private static final String CONTENT_TYPE = "application/octet-stream";
  private static Logger logger = LoggerFactory.getLogger(MinioService.class.getSimpleName());
  private MinioClient minioClient;
  private int numConnectionRetries;

  private RetryPolicy minioConnectionRetryPolicy =
      new RetryPolicy().retryOn(NoResponseException.class) // upon no response from server
          .retryOn(IOException.class) // upon connection error
          .retryOn(ErrorResponseException.class) // upon unsuccessful execution
          .abortOn(InvalidBucketNameException.class) // upon invalid bucket name
          .abortOn(InvalidKeyException.class) // upon an invalid access key or secret key
          .abortOn(XmlPullParserException.class) // upon parsing response XML
          .abortOn(InternalException.class) // upon internal library error
          .abortOn(NoSuchAlgorithmException.class) // upon requested algorithm was not found during
          // signature calculation
          .abortOn(InsufficientDataException.class) // Thrown to indicate that reading given InputStream
          // gets EOFException before reading given length.
          .withDelay(1, TimeUnit.SECONDS).withMaxRetries(numConnectionRetries);

  public MinioService(MinioClient minioClient, int numConnectionRetries) {
    this.minioClient = minioClient;
    this.numConnectionRetries = numConnectionRetries;
  }

  public static String minioCompatibleID(String id) {
    return id.replace(BenchFlowConstants.MODEL_ID_DELIMITER, MINIO_ID_DELIMITER);
  }

  public void initializeBuckets() {

    try {
      Failsafe.with(minioConnectionRetryPolicy).run(() -> {
        if (!minioClient.bucketExists(TESTS_BUCKET)) {
          minioClient.makeBucket(TESTS_BUCKET);
        }
      });

    } catch (Exception e) {
      // TODO - handle exception
      logger.error("Exception in initializeBuckets ", e);
    }
  }

  public void saveTestDefinition(String testID, InputStream definition) {

    logger.info("saveTestDefinition: " + testID);

    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.TEST_EXPERIMENT_DEFINITION_FILE_NAME;

    putInputStreamObject(definition, objectName);
  }

  public InputStream getTestDefinition(String testID) {

    logger.info("getTestDefinition: " + testID);

    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.TEST_EXPERIMENT_DEFINITION_FILE_NAME;

    return getInputStreamObject(objectName);
  }

  public void removeTestDefinition(String testID) {

    logger.info("removeTestDefinition: " + testID);

    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.TEST_EXPERIMENT_DEFINITION_FILE_NAME;

    removeObject(objectName);
  }

  public void saveTestDeploymentDescriptor(String testID, InputStream deploymentDescriptor) {

    logger.info("saveTestDeploymentDescriptor: " + testID);

    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.DEPLOYMENT_DESCRIPTOR_FILE_NAME;

    putInputStreamObject(deploymentDescriptor, objectName);
  }

  public void copyDeploymentDescriptorForExperiment(String testID, String experimentID) {

    logger.info("copyDeploymentDescriptorForExperiment: from:" + testID + " to:" + experimentID);

    String testObjectID = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.DEPLOYMENT_DESCRIPTOR_FILE_NAME;
    String experimentObjectID = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.DEPLOYMENT_DESCRIPTOR_FILE_NAME;

    copyObject(testObjectID, experimentObjectID);
  }

  public InputStream getTestDeploymentDescriptor(String testID) {

    logger.info("getTestDeploymentDescriptor: " + testID);

    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.DEPLOYMENT_DESCRIPTOR_FILE_NAME;

    return getInputStreamObject(objectName);
  }

  public void removeTestDeploymentDescriptor(String testID) {

    logger.info("removeTestDeploymentDescriptor: " + testID);

    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.DEPLOYMENT_DESCRIPTOR_FILE_NAME;

    removeObject(objectName);
  }

  public void saveTestBPMNModel(String testID, String modelName, InputStream model) {

    logger.info("saveTestBPMNModel: " + testID + MINIO_ID_DELIMITER + modelName);

    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.BPMN_MODELS_FOLDER_NAME + MINIO_ID_DELIMITER + modelName;

    putInputStreamObject(model, objectName);
  }

  public void copyBPMNModelForExperiment(String testID, String experimentID, String modelName) {

    logger.info("copyBPMNModelForExperiment: from:" + testID + " to:" + experimentID + " model:"
        + modelName);

    String testObjectID = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.BPMN_MODELS_FOLDER_NAME + MINIO_ID_DELIMITER + modelName;
    String experimentObjectID = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.BPMN_MODELS_FOLDER_NAME + MINIO_ID_DELIMITER + modelName;

    copyObject(testObjectID, experimentObjectID);
  }

  public InputStream getTestBPMNModel(String testID, String modelName) {

    logger.info("getTestBPMNModel: " + testID + MINIO_ID_DELIMITER + modelName);

    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.BPMN_MODELS_FOLDER_NAME + MINIO_ID_DELIMITER + modelName;

    return getInputStreamObject(objectName);
  }

  public List<String> getAllTestBPMNModels(final String testID) {

    logger.info("getAllTestBPMNModels: " + testID);

    List<String> modelNames = new ArrayList<>();

    final String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.BPMN_MODELS_FOLDER_NAME + MINIO_ID_DELIMITER;

    try {

      Failsafe.with(minioConnectionRetryPolicy).run(() -> {
        for (Result<Item> item : minioClient.listObjects(TESTS_BUCKET, objectName)) {
          modelNames.add(item.get().objectName().replace(objectName, ""));
        }
      });


    } catch (Exception e) {
      // TODO - handle exception
      logger.error("Exception in getAllTestBPMNModels: " + objectName, e);
    }

    return modelNames;
  }

  public void removeTestBPMNModel(String testID, String modelName) {

    logger.info("removeTestBPMNModel: " + testID + MINIO_ID_DELIMITER + modelName);

    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.BPMN_MODELS_FOLDER_NAME + MINIO_ID_DELIMITER + modelName;

    removeObject(objectName);
  }

  public void saveExperimentDefinition(String experimentID, InputStream definition) {

    logger.info("saveExperimentDefinition: " + experimentID);

    String objectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.TEST_EXPERIMENT_DEFINITION_FILE_NAME;

    putInputStreamObject(definition, objectName);
  }

  public InputStream getExperimentDefinition(String experimentID) {

    logger.info("getExperimentDefinition: " + experimentID);

    String objectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.TEST_EXPERIMENT_DEFINITION_FILE_NAME;

    return getInputStreamObject(objectName);
  }

  public void removeExperimentDefinition(String experimentID) {

    logger.info("removeExperimentDefinition: " + experimentID);

    String objectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.TEST_EXPERIMENT_DEFINITION_FILE_NAME;

    removeObject(objectName);
  }

  public void saveExperimentDeploymentDescriptor(String experimentID,
      InputStream deploymentDescriptor) {

    logger.info("saveExperimentDeploymentDescriptor: " + experimentID);

    String objectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.DEPLOYMENT_DESCRIPTOR_FILE_NAME;

    putInputStreamObject(deploymentDescriptor, objectName);
  }

  public InputStream getExperimentDeploymentDescriptor(String experimentID) {

    logger.info("getExperimentDeploymentDescriptor: " + experimentID);

    String objectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER
        + BenchFlowConstants.DEPLOYMENT_DESCRIPTOR_FILE_NAME;

    return getInputStreamObject(objectName);
  }

  private void putInputStreamObject(InputStream inputStream, String objectName) {

    logger.info("putInputStreamObject: " + objectName);

    try {

      Failsafe.with(minioConnectionRetryPolicy).run(() -> minioClient.putObject(TESTS_BUCKET,
          objectName, inputStream, inputStream.available(), CONTENT_TYPE));

    } catch (Exception e) {
      // TODO - handle exception
      logger.error("Exception in putInputStreamObject: " + objectName, e);
    }
  }

  private InputStream getInputStreamObject(String objectName) {

    logger.info("getInputStreamObject: " + objectName);

    try {

      return Failsafe.with(minioConnectionRetryPolicy)
          .get(() -> minioClient.getObject(TESTS_BUCKET, objectName));

    } catch (Exception e) {
      // TODO - handle exception
      logger.error("Exception in getInputStreamObject: " + objectName, e);
      return null;

    }
  }

  private void removeObject(String objectName) {

    logger.info("removeObject: " + objectName);

    try {
      Failsafe.with(minioConnectionRetryPolicy)
          .run(() -> minioClient.removeObject(TESTS_BUCKET, objectName));

    } catch (Exception e) {

      // TODO - handle ErrorResponseException happens if the object to remove doesn't exist, do nothing */
      //      logger.error("Exception in removeObject: " + objectName, e);

      // TODO - handle exception
      logger.error("Exception in removeObject: " + objectName, e);
    }
  }

  private void copyObject(String fromObjectName, String toObjectName) {

    logger.info("copyObject: from:" + fromObjectName + " to:" + toObjectName);

    try {
      Failsafe.with(minioConnectionRetryPolicy).run(() -> {

        // the provided copyObject does not seem to work, so we do this workaround
        // minioClient.copyObject(TESTS_BUCKET, fromObjectName, TESTS_BUCKET, toObjectName);

        // convert to buffered input stream as the type minio returns cannot be put
        String temp = IOUtils.toString(minioClient.getObject(TESTS_BUCKET, fromObjectName),
            StandardCharsets.UTF_8);
        InputStream tempInputStream = IOUtils.toInputStream(temp, StandardCharsets.UTF_8);

        minioClient.putObject(TESTS_BUCKET, toObjectName, tempInputStream,
            tempInputStream.available(), CONTENT_TYPE);

      });

    } catch (Exception e) {
      // TODO - handle exception
      logger.error("Exception in copyObject: from:" + fromObjectName + " to:" + toObjectName, e);
    }
  }
}
