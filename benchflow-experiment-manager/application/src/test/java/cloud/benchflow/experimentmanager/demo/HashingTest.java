package cloud.benchflow.experimentmanager.demo;

import cloud.benchflow.experimentmanager.helpers.TestConstants;
import org.junit.Test;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MINIO_ID_DELIMITER;
import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-13
 */
public class HashingTest {
    @Test
    public void hashKey() throws Exception {

        // TODO - remove me after demo

        String[] experimentIDArray = TestConstants.BENCHFLOW_EXPERIMENT_ID.split("\\.");

        final String experimentName = "ParallelMultiple11Activiti5210";
        final long experimentNumber = 1;
        final String driversMakerExperimentID = "BenchFlow." + experimentName + MODEL_ID_DELIMITER + experimentNumber;
        final String minioExperimentID = driversMakerExperimentID.replace(MODEL_ID_DELIMITER, MINIO_ID_DELIMITER);

        System.out.println("DriversMaker ID: " + driversMakerExperimentID);
        System.out.println("Minio ID: " + minioExperimentID);
        System.out.println("Hashed Minio ID: " + Hashing.hashKey(minioExperimentID));

        System.out.println("test: " + Hashing.hashKey("BenchFlow.ParallelMultiple11Activiti5210".replace(MODEL_ID_DELIMITER, MINIO_ID_DELIMITER)));

    }

}