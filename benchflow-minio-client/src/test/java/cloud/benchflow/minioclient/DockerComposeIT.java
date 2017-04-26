package cloud.benchflow.minioclient;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.DockerMachine;
import com.palantir.docker.compose.connection.DockerPort;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.ClassRule;

public class DockerComposeIT {

  private static String MINIO_NAME = "minio";
  private static int MINIO_PORT = 9000;

  private static String MINIO_TAG = "RELEASE.2017-02-16T01-47-30Z";
  static String MINIO_ACCESS_KEY = "minio";
  static String MINIO_SECRET_KEY = "minio123";

  private static final DockerMachine dockerMachine =
      DockerMachine.localMachine()
          .withAdditionalEnvironmentVariable("MINIO_TAG", MINIO_TAG)
          .withAdditionalEnvironmentVariable("MINIO_ACCESS_KEY", MINIO_ACCESS_KEY)
          .withAdditionalEnvironmentVariable("MINIO_SECRET_KEY", MINIO_SECRET_KEY)
          .build();

  @ClassRule
  public static DockerComposeRule dockerComposeRule =
      DockerComposeRule.builder()
          .file("src/test/resources/docker-compose/docker-compose.yml")
          .machine(dockerMachine)
          .build();

  static DockerPort MINIO_CONTAINER;

  @BeforeClass
  public static void prepareContainers() throws IOException {
    MINIO_CONTAINER = dockerComposeRule.containers().container(MINIO_NAME).port(MINIO_PORT);
  }
}
