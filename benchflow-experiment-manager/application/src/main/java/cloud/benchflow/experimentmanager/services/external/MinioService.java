package cloud.benchflow.experimentmanager.services.external;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.*;
import static cloud.benchflow.experimentmanager.demo.Hashing.hashKey;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.MinioException;
import io.minio.errors.NoResponseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 05.03.17.
 */
public class MinioService {

  // http://www.iana.org/assignments/media-types/application/octet-stream
  private static final String CONTENT_TYPE = "application/octet-stream";
  private static Logger logger = LoggerFactory.getLogger(MinioService.class.getSimpleName());
  private MinioClient minioClient;

  public MinioService(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  public void initializeBuckets() {

    try {
      if (!minioClient.bucketExists(TESTS_BUCKET)) {
        minioClient.makeBucket(TESTS_BUCKET);
      }

    } catch (InvalidBucketNameException | NoSuchAlgorithmException | IOException
        | InsufficientDataException | InvalidKeyException | NoResponseException
        | XmlPullParserException | ErrorResponseException | InternalException e) {
      // TODO - handle exception
      logger.error("Exception in initializeBuckets ", e);
    }
  }

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

    } catch (MinioException | NoSuchAlgorithmException | XmlPullParserException | IOException
        | InvalidKeyException e) {
      logger.error(e.getMessage());
      return false;
    }
  }

  public void saveExperimentDefinition(String experimentID, InputStream definitionInputStream) {

    logger.info("saveExperimentDefinition: " + experimentID);

    String objectName =
        minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + PT_PE_DEFINITION_FILE_NAME;

    putInputStreamObject(definitionInputStream, objectName);
  }

  public InputStream getExperimentDefinition(String experimentID) {

    logger.info("getExperimentDefinition: " + experimentID);

    String objectName =
        minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + PT_PE_DEFINITION_FILE_NAME;

    return getInputStreamObject(objectName);
  }

  public void copyExperimentDefintionForDriversMaker(String driversMakerExperimentID,
      long experimentNumber, InputStream definitionInputStream) {

    // TODO - change/remove this method when DriversMaker changes

    logger.info("copyExperimentDefintionForDriversMaker: " + driversMakerExperimentID
        + MODEL_ID_DELIMITER + experimentNumber);

    String objectName = minioCompatibleID(driversMakerExperimentID);

    try {

      String hashedObjectName = hashKey(objectName) + MINIO_ID_DELIMITER + experimentNumber
          + MINIO_ID_DELIMITER + PT_PE_DEFINITION_FILE_NAME;
      putInputStreamObject(definitionInputStream, hashedObjectName);

    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
      logger.error(e.getMessage());
    }
  }

  public void saveExperimentDeploymentDescriptor(String experimentID,
      InputStream definitionInputStream) {

    logger.info("saveExperimentDeploymentDescriptor: " + experimentID);

    String objectName =
        minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + DEPLOYMENT_DESCRIPTOR_FILE_NAME;

    putInputStreamObject(definitionInputStream, objectName);
  }

  public InputStream getExperimentDeploymentDescriptor(String experimentID) {

    // TODO - change/remove this method when DriversMaker changes

    logger.info("getExperimentDeploymentDescriptor: " + experimentID);

    String objectName =
        minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + DEPLOYMENT_DESCRIPTOR_FILE_NAME;

    return getInputStreamObject(objectName);
  }

  public void copyDeploymentDescriptorForDriversMaker(String experimentID,
      String driversMakerExperimentID, long experimentNumber) {

    // TODO - change/remove this method when DriversMaker changes

    logger.info("copyDeploymentDescriptorForDriversMaker: " + experimentID + MODEL_ID_DELIMITER
        + experimentNumber);

    String experimentObjectName =
        minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + DEPLOYMENT_DESCRIPTOR_FILE_NAME;

    try {

      String driversMakerObjectName =
          hashKey(minioCompatibleID(driversMakerExperimentID)) + MINIO_ID_DELIMITER
              + experimentNumber + MINIO_ID_DELIMITER + DEPLOYMENT_DESCRIPTOR_FILE_NAME;
      copyObject(experimentObjectName, driversMakerObjectName);

    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
      logger.error(e.getMessage());
    }
  }

  public void saveExperimentBPMNModel(String experimentID, String modelName,
      InputStream definitionInputStream) {

    logger.info("saveExperimentBPMNModel: " + experimentID);

    String objectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER
        + BPMN_MODELS_FOLDER_NAME + MINIO_ID_DELIMITER + modelName;

    putInputStreamObject(definitionInputStream, objectName);
  }

  public InputStream getExperimentBPMNModel(String experimentID, String modelName) {

    // TODO - change/remove this method when DriversMaker changes

    logger.info("getExperimentBPMNModel: " + experimentID + MINIO_ID_DELIMITER + modelName);

    String testID = experimentID.substring(0, experimentID.lastIndexOf("."));

    String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER + BPMN_MODELS_FOLDER_NAME
        + MINIO_ID_DELIMITER + modelName;

    return getInputStreamObject(objectName);
  }

  public void copyExperimentBPMNModelForDriversMaker(String experimentID,
      String driversMakerExperimentID, String modelName) {

    // TODO - change/remove this method when DriversMaker changes

    logger.info("copyExperimentBPMNModelForDriversMaker: " + driversMakerExperimentID
        + MINIO_ID_DELIMITER + modelName);

    String experimentObjectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER
        + BPMN_MODELS_FOLDER_NAME + MINIO_ID_DELIMITER + modelName;

    try {
      String driversMakerObjectName = hashKey(minioCompatibleID(driversMakerExperimentID))
          + MINIO_ID_DELIMITER + BPMN_MODELS_FOLDER_NAME + MINIO_ID_DELIMITER + modelName;
      copyObject(experimentObjectName, driversMakerObjectName);
    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
      logger.error(e.getMessage());
    }
  }

  public InputStream getDriversMakerGeneratedBenchmark(String driversMakerExperimentID,
      long experimentNumber) {

    // TODO - change this method when DriversMaker changes

    logger.info("getDriversMakerGeneratedBenchmark: " + driversMakerExperimentID
        + MODEL_ID_DELIMITER + experimentNumber);

    String objectName = minioCompatibleID(driversMakerExperimentID);

    try {

      String hashedObjectName = hashKey(objectName) + MINIO_ID_DELIMITER + experimentNumber
          + MINIO_ID_DELIMITER + GENERATED_BENCHMARK_FILENAME;
      return getInputStreamObject(hashedObjectName);

    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
      logger.error(e.getMessage());
    }

    return null;
  }

  public InputStream getDriversMakerGeneratedFabanConfiguration(String driversMakerExperimentID,
      long experimentNumber, long trialNumber) {

    // TODO - change this method when DriversMaker changes

    logger.info("getDriversMakerGeneratedFabanConfiguration: " + driversMakerExperimentID
        + MODEL_ID_DELIMITER + experimentNumber + MODEL_ID_DELIMITER + trialNumber);

    String objectName = minioCompatibleID(driversMakerExperimentID);

    try {
      String hashedObjectName = hashKey(objectName) + MINIO_ID_DELIMITER + experimentNumber
          + MINIO_ID_DELIMITER + trialNumber + MINIO_ID_DELIMITER + FABAN_CONFIG_FILENAME;
      return getInputStreamObject(hashedObjectName);

    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return null;
  }

  private void putInputStreamObject(InputStream inputStream, String objectName) {

    logger.info("putInputStreamObject: " + objectName);

    try {

      minioClient.putObject(TESTS_BUCKET, objectName, inputStream, inputStream.available(),
          CONTENT_TYPE);

      logger.info("putInputStreamObject: added ");

    } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException
        | IOException | NoResponseException | InvalidKeyException | ErrorResponseException
        | XmlPullParserException | InvalidArgumentException | InternalException e) {
      // TODO - handle exception
      logger.error("Exception in putInputStreamObject: " + objectName, e);
    }
  }

  private InputStream getInputStreamObject(String objectName) {

    logger.info("getInputStreamObject: " + objectName);

    try {

      return minioClient.getObject(TESTS_BUCKET, objectName);

    } catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException
        | IOException | InvalidKeyException | NoResponseException | XmlPullParserException
        | InternalException | InvalidArgumentException e) {
      // TODO - handle exception
      logger.error("Exception in getInputStreamObject: " + objectName, e);
      return null;

    } catch (ErrorResponseException e) {
      /* happens if the object doesn't exist*/
      return null;
    }
  }

  private void copyObject(String fromObjectName, String toObjectName) {

    logger.info("copyObject: from:" + fromObjectName + " to:" + toObjectName);

    try {
      // the provided copyObject does not seem to work, so we do this workaround
      // minioClient.copyObject(TESTS_BUCKET, fromObjectName, TESTS_BUCKET, toObjectName);

      // convert to buffered input stream as the type minio returns cannot be put
      String temp = IOUtils.toString(minioClient.getObject(TESTS_BUCKET, fromObjectName),
          StandardCharsets.UTF_8);
      InputStream tempInputStream = IOUtils.toInputStream(temp, StandardCharsets.UTF_8);

      minioClient.putObject(TESTS_BUCKET, toObjectName, tempInputStream,
          tempInputStream.available(), CONTENT_TYPE);

    } catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException
        | InsufficientDataException | NoResponseException | ErrorResponseException
        | InternalException | IOException | XmlPullParserException | InvalidArgumentException e) {
      // TODO - handle exception
      logger.error("Exception in copyObject: from:" + fromObjectName + " to:" + toObjectName, e);
    }
  }

  private String minioCompatibleID(String id) {
    return id.replace(MODEL_ID_DELIMITER, MINIO_ID_DELIMITER);
  }
}
