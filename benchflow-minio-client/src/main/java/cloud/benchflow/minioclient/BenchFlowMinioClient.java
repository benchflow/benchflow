package cloud.benchflow.minioclient;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public abstract class BenchFlowMinioClient {

  // TODO - is this correct with only one bucket and it is called tests? Maybe better then just 'benchflow'?
  protected static final String TESTS_BUCKET = "tests";

  private static final String DEPLOYMENT_DESCRIPTOR_NAME = "docker-compose";
  private static final String PT_PE_DEFINITION_NAME = "benchflow-test";
  private static final String YAML_EXTENSION = ".yml";
  protected static final String PT_PE_DEFINITION_FILE_NAME = PT_PE_DEFINITION_NAME + YAML_EXTENSION;
  protected static final String DEPLOYMENT_DESCRIPTOR_FILE_NAME =
      DEPLOYMENT_DESCRIPTOR_NAME + YAML_EXTENSION;

  protected static final String BPMN_MODELS_FOLDER_NAME = "models";
  protected static final String GENERATED_BENCHMARK_FILENAME = "benchflow-benchmark.jar";
  protected static final String FABAN_CONFIG_FILENAME = "run.xml";

  protected static final String MINIO_ID_DELIMITER = "/";
  public static final String MODEL_ID_DELIMITER = ".";

  // http://www.iana.org/assignments/media-types/application/octet-stream
  private static final String CONTENT_TYPE = "application/octet-stream";

  private static Logger logger =
      LoggerFactory.getLogger(BenchFlowMinioClient.class.getSimpleName());

  protected MinioClient minioClient;

  public BenchFlowMinioClient(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  public void initializeBuckets() {
    try {
      if (!minioClient.bucketExists(TESTS_BUCKET)) {
        minioClient.makeBucket(TESTS_BUCKET);
      }
    } catch (InvalidBucketNameException
        | NoSuchAlgorithmException
        | IOException
        | InsufficientDataException
        | InvalidKeyException
        | NoResponseException
        | XmlPullParserException
        | ErrorResponseException
        | InternalException
        | RegionConflictException e) {
      // TODO - handle exception
      logger.error("Exception in initializeBuckets ", e);
    }
  }

  /**
   * @param inputStream
   * @param objectName
   */
  protected void putInputStreamObject(InputStream inputStream, String objectName) {
    logger.info("putInputStreamObject: " + objectName);
    try {
      minioClient.putObject(
          TESTS_BUCKET, objectName, inputStream, inputStream.available(), CONTENT_TYPE);

      logger.info("putInputStreamObject: added ");
    } catch (InvalidBucketNameException
        | NoSuchAlgorithmException
        | InsufficientDataException
        | IOException
        | NoResponseException
        | InvalidKeyException
        | ErrorResponseException
        | XmlPullParserException
        | InvalidArgumentException
        | InternalException e) {
      // TODO - handle exception
      logger.error("Exception in putInputStreamObject: " + objectName, e);
    }
  }

  /**
   * @param objectName
   * @return
   */
  protected InputStream getInputStreamObject(String objectName) {
    logger.info("getInputStreamObject: " + objectName);
    try {
      return minioClient.getObject(TESTS_BUCKET, objectName);
    } catch (InvalidBucketNameException
        | NoSuchAlgorithmException
        | InsufficientDataException
        | IOException
        | InvalidKeyException
        | NoResponseException
        | XmlPullParserException
        | InternalException
        | InvalidArgumentException e) {
      // TODO - handle exception
      logger.error("Exception in getInputStreamObject: " + objectName, e);
      return null;
    } catch (ErrorResponseException e) {
      /* happens if the object doesn't exist*/
      return null;
    }
  }

  void removeObject(String objectName) {
    logger.info("removeObject: " + objectName);
    try {
      minioClient.removeObject(TESTS_BUCKET, objectName);
    } catch (InvalidBucketNameException
        | NoSuchAlgorithmException
        | InsufficientDataException
        | IOException
        | InvalidKeyException
        | NoResponseException
        | XmlPullParserException
        | InternalException e) {
      // TODO - handle exception
      logger.error("Exception in removeObject: " + objectName, e);
    } catch (ErrorResponseException e) {
      /* happens if the object to remove doesn't exist, do nothing */
    }
  }

  void copyObject(String fromObjectName, String toObjectName) {
    logger.info("copyObject: from:" + fromObjectName + " to:" + toObjectName);
    try {
      // the provided copyObject does not seem to work, so we do this workaround
      // minioClient.copyObject(TESTS_BUCKET, fromObjectName, TESTS_BUCKET, toObjectName);

      // convert to buffered input stream as the type minio returns cannot be put
      String temp =
          IOUtils.toString(
              minioClient.getObject(TESTS_BUCKET, fromObjectName), StandardCharsets.UTF_8);
      InputStream tempInputStream = IOUtils.toInputStream(temp, StandardCharsets.UTF_8);

      minioClient.putObject(
          TESTS_BUCKET, toObjectName, tempInputStream, tempInputStream.available(), CONTENT_TYPE);
    } catch (InvalidKeyException
        | InvalidBucketNameException
        | NoSuchAlgorithmException
        | InsufficientDataException
        | NoResponseException
        | ErrorResponseException
        | InternalException
        | IOException
        | XmlPullParserException
        | InvalidArgumentException e) {
      // TODO - handle exception
      logger.error("Exception in copyObject: from:" + fromObjectName + " to:" + toObjectName, e);
    }
  }

  protected String minioCompatibleID(String id) {
    return id.replace(MODEL_ID_DELIMITER, MINIO_ID_DELIMITER);
  }
}
