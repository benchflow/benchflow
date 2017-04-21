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
 *         created on 02.03.17.
 */
public class DockerComposeIT {

    private static String MONGO_NAME = "mongo";
    private static int MONGO_PORT = 27017;

    private static String MINIO_NAME = "minio";
    private static int MINIO_PORT = 9000;
    public static String MINIO_ACCESS_KEY = "minio";
    public static String MINIO_SECRET_KEY = "minio123";

    private static String MONGO_DATA_VOLUME_PATH = System.getProperty("user.dir") + "/src/test/resources/docker-compose/mongo-data";

    private static final DockerMachine dockerMachine = DockerMachine.localMachine()
            .withAdditionalEnvironmentVariable("MONGO_TAG", "3.4.2")
            .withAdditionalEnvironmentVariable("MONGO_DATA_VOLUME", MONGO_DATA_VOLUME_PATH)
            .withAdditionalEnvironmentVariable("MINIO_TAG", "RELEASE.2017-02-16T01-47-30Z")
            .withAdditionalEnvironmentVariable("MINIO_ACCESS_KEY", MINIO_ACCESS_KEY)
            .withAdditionalEnvironmentVariable("MINIO_SECRET_KEY", MINIO_SECRET_KEY)
            .build();

    // to wait for a service to be available see https://github.com/palantir/docker-compose-rule#waiting-for-a-service-to-be-available
    // can also be specified in docker compose as a health check
    @ClassRule
    public static DockerComposeRule dockerComposeRule = DockerComposeRule.builder()
            .file("src/test/resources/docker-compose/docker-compose.yml")
            .machine(dockerMachine)
            .waitingForService(MONGO_NAME, HealthChecks.toHaveAllPortsOpen())
            .waitingForService(MINIO_NAME, HealthChecks.toHaveAllPortsOpen())
            .build();

    public static DockerPort MONGO_CONTAINER;
    public static DockerPort MINIO_CONTAINER;


    @BeforeClass
    public static void prepareContainers() throws IOException {
        MONGO_CONTAINER = dockerComposeRule.containers().container(MONGO_NAME).port(MONGO_PORT);
        MINIO_CONTAINER = dockerComposeRule.containers().container(MINIO_NAME).port(MINIO_PORT);

        // remove possible previous files
        FileUtils.cleanDirectory(new File(MONGO_DATA_VOLUME_PATH));
    }

    @AfterClass
    public static void cleanUpContainers() throws IOException {

        FileUtils.cleanDirectory(new File(MONGO_DATA_VOLUME_PATH));

    }
}
