package cloud.benchflow.minioclient;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.DockerMachine;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.*;

import java.io.IOException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 * @author Vincenzo Ferme (fermevincenzo@gmail.com) created on 02.03.17.
 */
public class DockerComposeIT {

  private static String MINIO_NAME = getEnvOrDefault("MINIO_NAME", "minio");
  private static String MINIO_HOST = getEnvOrDefault("MINIO_PORT_9000_TCP_ADDR", "localhost");
  private static int MINIO_PORT =
      Integer.valueOf(getEnvOrDefault("MINIO_PORT_9000_TCP_PORT", "9000"));
  protected static String MINIO_ACCESS_KEY = getEnvOrDefault("MINIO_ACCESS_KEY", "minio");
  protected static String MINIO_SECRET_KEY = getEnvOrDefault("MINIO_SECRET_KEY", "minio123");
  private static String MINIO_TAG = getEnvOrDefault("MINIO_TAG", "RELEASE.2017-02-16T01-47-30Z");

  private static String LOCAL_DOCKER_COMPOSE_PATH =
      "src/test/resources/docker-compose/docker-compose.yml";

  protected static DockerPort MINIO_CONTAINER;

  // boolean to keep track if we are running in Continuous Integration or not
  // by relying on the CI env variable
  // IMPORTANT: needs to be executed before setupDockerMachineIfLocal() and setupDockerComposeIfLocal(),
  // otherwise it is always false
  private static boolean inLocal = System.getenv("CI") == null;

  // dockerComposeRule and dockerMachine are used only when executing the local workflow
  // IMPORTANT: needs to be executed before setupDockerComposeIfLocal()
  private static final DockerMachine dockerMachine = setupDockerMachineIfLocal();

  @ClassRule public static DockerComposeRule dockerComposeRule = setupDockerComposeIfLocal();

  // this seems to happen every time an IT test is executed but the variable assignment of the above variables survive
  @BeforeClass
  public static void prepareContainers() throws IOException, InterruptedException {

    ensureServicesAreReady();
  }

  // ensure that services are ready to be used for testing
  private static void ensureServicesAreReady() throws InterruptedException {

    System.out.println("============== ensureServicesAreReady =====================");

    if (MINIO_CONTAINER != null) {
      // if container variables are already set we can return
      return;
    }

    if (inLocal) {

      // We make sure that the host and the container port are the same by defining it in docker compose
      MINIO_CONTAINER = dockerComposeRule.containers().container(MINIO_NAME).port(MINIO_PORT);

    } else {

      // We make sure that the host and the container port are the same by defining it in wercker
      MINIO_CONTAINER = new DockerPort(MINIO_HOST, MINIO_PORT, MINIO_PORT);

      //The following is not usable, even if cool: https://github.com/palantir/docker-compose-rule/blob/8cce225f6d434cb47b2b09c089871b48bc83897b/docker-compose-rule-core/src/main/java/com/palantir/docker/compose/connection/DockerPort.java
      //because it depends on docker clusters that we do not start

      // We cannot use a ClassRule because: The @ClassRule 'waitingForServices' must return an implementation of TestRule.

      //Handle the wait for a port to be reachable, one by one, with maximum retry
      int maxRetries = 10;

      //Waiting check interval
      int checkIntervalInMs = 10000;

      System.out.println("Waiting for Minio...");

      //Wait for Minio
      waitForPortAvailabilityOrFail(MINIO_CONTAINER, maxRetries, checkIntervalInMs);

      System.out.println("Minio is available!");
    }
  }

  // setup docker machine if we are in the local workflow
  private static DockerMachine setupDockerMachineIfLocal() {

    System.out.println("============== setupDockerMachineIfLocal =====================");

    if (inLocal) {

      //We rely on the CI env variable to detect if we are not in CI environment
      return DockerMachine.localMachine()
          .withAdditionalEnvironmentVariable("MINIO_TAG", MINIO_TAG)
          .withAdditionalEnvironmentVariable("MINIO_ACCESS_KEY", MINIO_ACCESS_KEY)
          .withAdditionalEnvironmentVariable("MINIO_SECRET_KEY", MINIO_SECRET_KEY)
          .build();
    }

    return null;
  }

  // setup docker compose if we are in the local workflow
  private static DockerComposeRule setupDockerComposeIfLocal() {

    System.out.println("============== setupDockerComposeIfLocal =====================");

    if (inLocal) {

      // to wait for a service to be available see https://github.com/palantir/docker-compose-rule#waiting-for-a-service-to-be-available
      // can also be specified in docker compose as a health check
      return dockerComposeRule =
          DockerComposeRule.builder()
              .file(LOCAL_DOCKER_COMPOSE_PATH)
              .machine(dockerMachine)
              .waitingForService(MINIO_NAME, HealthChecks.toHaveAllPortsOpen())
              .build();
    }

    return null;
  }

  // wait for a port to be available
  // true = available, false = not available after maxRetries
  private static void waitForPortAvailabilityOrFail(
      DockerPort port, int maxRetries, int checkIntervalInMs) throws InterruptedException {

    while (maxRetries > 0) {

      Thread.sleep(checkIntervalInMs);

      if (port.isListeningNow()) {
        return;
      }

      maxRetries--;
    }

    Assert.fail("could not connect to " + port + " after " + maxRetries + " retries.");
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
