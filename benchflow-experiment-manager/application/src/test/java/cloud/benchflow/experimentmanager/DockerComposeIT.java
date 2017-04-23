package cloud.benchflow.experimentmanager;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.DockerMachine;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import java.io.File;
import java.io.IOException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 * @author Vincenzo Ferme (fermevincenzo@gmail.com)
 *         created on 02.03.17.
 */
public class DockerComposeIT {

    private static String MONGO_NAME = getEnvOrDefault("MONGO_NAME", "mongo");
    private static String MONGO_HOST = getEnvOrDefault("MONGO_PORT_27017_TCP_ADDR", "localhost");
    private static int MONGO_PORT = Integer.valueOf(getEnvOrDefault("MONGO_PORT_27017_TCP_PORT", "27017"));
    public static String MONGO_TAG = getEnvOrDefault("MONGO_TAG", "3.4.2");

    private static String MINIO_NAME = getEnvOrDefault("MINIO_NAME", "minio");
    private static String MINIO_HOST = getEnvOrDefault("MINIO_PORT_9000_TCP_ADDR", "localhost");
    private static int MINIO_PORT = Integer.valueOf(getEnvOrDefault("MINIO_PORT_9000_TCP_PORT", "9000"));
    public static String MINIO_ACCESS_KEY = getEnvOrDefault("MINIO_ACCESS_KEY", "minio");
    public static String MINIO_SECRET_KEY = getEnvOrDefault("MINIO_SECRET_KEY", "minio123");
    public static String MINIO_TAG = getEnvOrDefault("MINIO_TAG", "RELEASE.2017-02-16T01-47-30Z");

    private static String LOCAL_MONGO_DATA_VOLUME_PATH = System.getProperty("user.dir") + "/src/test/resources/docker-compose/mongo-data";
    private static String MONGO_DATA_VOLUME_PATH = getEnvOrDefault("MONGO_DATA_VOLUME_PATH", LOCAL_MONGO_DATA_VOLUME_PATH);
     
    public static DockerPort MONGO_CONTAINER;
    public static DockerPort MINIO_CONTAINER;
    
    // dockerComposeRule and dockerMachine are used only when executing the local workflow
    private static final DockerMachine dockerMachine = setupDockerMachineIfLocal();
    @ClassRule
    public static DockerComposeRule dockerComposeRule = setupDockerComposeIfLocal();

    // this seems to happen every time an IT test is executed but the variable assignment of the above variables survive
    @BeforeClass
    public static void prepareContainers() throws IOException, InterruptedException {

        ensureServicesAreReady();

        // create the mongo data directory if not present
        FileUtils.forceMkdir(new File(MONGO_DATA_VOLUME_PATH));

        // remove possible previous files
        FileUtils.cleanDirectory(new File(MONGO_DATA_VOLUME_PATH));
    }

    // ensure that services are ready to be used for testing
    private static void ensureServicesAreReady() throws InterruptedException {

        String CI = getEnvOrDefault("CI", "false");

        if ((CI.matches("false")) &&
                (MONGO_CONTAINER == null || MINIO_CONTAINER == null)) {

            // We make sure that the host and the container port are the same by defining it in docker compose
            MONGO_CONTAINER = dockerComposeRule.containers().container(MONGO_NAME).port(MONGO_PORT);
            MINIO_CONTAINER = dockerComposeRule.containers().container(MINIO_NAME).port(MINIO_PORT);

        } else if ((CI.matches("true")) &&
                        (MONGO_CONTAINER == null || MINIO_CONTAINER == null)) {

            // We make sure that the host and the container port are the same by defining it in wercker
            MONGO_CONTAINER = new DockerPort(MONGO_HOST,MONGO_PORT,MONGO_PORT);
            MINIO_CONTAINER = new DockerPort(MINIO_HOST,MINIO_PORT,MINIO_PORT);

            //The following is not usable, even if cool: https://github.com/palantir/docker-compose-rule/blob/8cce225f6d434cb47b2b09c089871b48bc83897b/docker-compose-rule-core/src/main/java/com/palantir/docker/compose/connection/DockerPort.java
            //because it depends on docker clusters that we do not start

            // We cannot use a ClassRule because: The @ClassRule 'waitingForServices' must return an implementation of TestRule.

            //Handle the wait for a port to be reachable, one by one, with maximum retry
            int maxRetries = 10;

            //Waiting check interval
            int checkIntervalInMs = 10000;

            System.out.println("Waiting for Mongo...");

            //Wait for Mongo
            waitingForPortAvailability(MONGO_CONTAINER, maxRetries, checkIntervalInMs);

            System.out.println("Mongo is available!");
            System.out.println("Waiting for Minio...");

            //Wait for Minio
            waitingForPortAvailability(MONGO_CONTAINER, maxRetries, checkIntervalInMs);

            System.out.println("Minio is available!");
        }
    }

    @AfterClass
    public static void cleanUpContainers() throws IOException {

        FileUtils.cleanDirectory(new File(MONGO_DATA_VOLUME_PATH));

    }

    // setup docker machine if we are in the local workflow
    private static DockerMachine setupDockerMachineIfLocal() {

        //We rely on the CI env variable to detect if we are not in CI environment
        String CI = getEnvOrDefault("CI", "false");

        if ((CI.matches("false")) &&
                (dockerComposeRule == null || dockerMachine == null)) {

            //We rely on the CI env variable to detect if we are not in CI environment
            return DockerMachine.localMachine()
                    .withAdditionalEnvironmentVariable("MONGO_TAG", MONGO_TAG)
                    .withAdditionalEnvironmentVariable("MONGO_DATA_VOLUME", MONGO_DATA_VOLUME_PATH)
                    .withAdditionalEnvironmentVariable("MINIO_TAG", MINIO_TAG)
                    .withAdditionalEnvironmentVariable("MINIO_ACCESS_KEY", MINIO_ACCESS_KEY)
                    .withAdditionalEnvironmentVariable("MINIO_SECRET_KEY", MINIO_SECRET_KEY)
                    .build();

        } else {
            return null;
        }

    }

    // setup docker compose if we are in the local workflow
    private static DockerComposeRule setupDockerComposeIfLocal() {

        //We rely on the CI env variable to detect if we are not in CI environment
        String CI = getEnvOrDefault("CI", "false");

        if ((CI.matches("false")) &&
                (dockerComposeRule == null || dockerMachine == null)) {

            // to wait for a service to be available see https://github.com/palantir/docker-compose-rule#waiting-for-a-service-to-be-available
            // can also be specified in docker compose as a health check
            return dockerComposeRule = DockerComposeRule.builder()
                            .file("src/test/resources/docker-compose/docker-compose.yml")
                            .machine(dockerMachine)
                            .waitingForService(MONGO_NAME, HealthChecks.toHaveAllPortsOpen())
                            .waitingForService(MINIO_NAME, HealthChecks.toHaveAllPortsOpen())
                            .build();

        } else {
            return null;
        }

    }

    // wait for a port to be available
    // true = available, false = not available after maxRetries
    private static boolean waitingForPortAvailability(DockerPort port, int maxRetries, int checkIntervalInMs) throws InterruptedException {

        while (maxRetries>0) {

            Thread.sleep(checkIntervalInMs);

            if (port.isListeningNow()) {
                return true;
            } else {
                maxRetries--;
            }
        }

        return false;

    }

    // return the requested environment variable or a passed default
    private static String getEnvOrDefault(String env, String defaultValue) {

        String envValue = System.getenv(env);

        if (envValue != null) {
            return envValue;
        } else {
            return defaultValue;
        }

    }

}
