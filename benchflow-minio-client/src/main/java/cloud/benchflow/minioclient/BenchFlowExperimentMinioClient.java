package cloud.benchflow.minioclient;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-19
 */
public class BenchFlowExperimentMinioClient extends BenchFlowMinioClient {

    private static Logger logger = LoggerFactory.getLogger(BenchFlowExperimentMinioClient.class.getSimpleName());

    public BenchFlowExperimentMinioClient(MinioClient minioClient) {
        super(minioClient);
    }

    public boolean isValidExperimentID(String experimentID) {

        logger.info("isValidExperimentID: " + experimentID);

        String objectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + PT_PE_DEFINITION_FILE_NAME;

        try {

            boolean valid = minioClient.bucketExists(TESTS_BUCKET);

            if (valid) {

                valid = minioClient.listObjects(TESTS_BUCKET, objectName).iterator().hasNext();
            }

            return valid;

        } catch (MinioException | NoSuchAlgorithmException | XmlPullParserException | IOException | InvalidKeyException e) {
            logger.error(e.getMessage());
            return false;
        }

    }

    public void saveExperimentDefinition(String experimentID, InputStream definitionInputStream) {

        logger.info("getExperimentDefinition: " + experimentID);

        String objectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + PT_PE_DEFINITION_FILE_NAME;

        putInputStreamObject(definitionInputStream, objectName);
    }

    public InputStream getExperimentDefinition(String experimentID) {

        logger.info("getExperimentDefinition: " + experimentID);

        String objectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + PT_PE_DEFINITION_FILE_NAME;

        return getInputStreamObject(objectName);
    }


    public InputStream getExperimentDeploymentDescriptor(String experimentID) {

        logger.info("getExperimentDeploymentDescriptor: " + experimentID);

        String objectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + DEPLOYMENT_DESCRIPTOR_FILE_NAME;

        return getInputStreamObject(objectName);

    }

    public InputStream getExperimentBPMNModel(String experimentID, String modelName) {

        logger.info("getExperimentBPMNModel: " + experimentID + MINIO_ID_DELIMITER + modelName);

        String testID = experimentID.substring(0, experimentID.lastIndexOf("."));

        String objectName = minioCompatibleID(testID) + MINIO_ID_DELIMITER + BPMN_MODELS_FOLDER_NAME + MINIO_ID_DELIMITER + modelName;

        return getInputStreamObject(objectName);

    }

    public InputStream getDriversMakerGeneratedBenchmark(String experimentID) {

        logger.info("getDriversMakerGeneratedBenchmark: " + experimentID);

        String objectName = minioCompatibleID(experimentID) + MINIO_ID_DELIMITER + GENERATED_BENCHMARK_FILENAME;

        return getInputStreamObject(objectName);

    }

    public InputStream getDriversMakerGeneratedFabanConfiguration(String trialID) {

        logger.info("getDriversMakerGeneratedFabanConfiguration: " + trialID);

        String objectName = minioCompatibleID(trialID) + MINIO_ID_DELIMITER + FABAN_CONFIG_FILENAME;

        return getInputStreamObject(objectName);

    }

}
