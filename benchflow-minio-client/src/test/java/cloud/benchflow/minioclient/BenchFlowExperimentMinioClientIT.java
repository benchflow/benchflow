package cloud.benchflow.minioclient;

import static cloud.benchflow.minioclient.helpers.TestConstants.VALID_EXPERIMENT_ID;

import cloud.benchflow.minioclient.helpers.TestFiles;
import io.minio.MinioClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BenchFlowExperimentMinioClientIT extends DockerComposeIT {

  private BenchFlowExperimentMinioClient minioClient;

  private InputStream experimentDefinitionInputStream;

  @Before
  public void setUp() throws Exception {

    // TODO - see how to mock final class MinioClient
    // https://github.com/mockito/mockito/wiki/What%27s-new-in-Mockito-2#mock-the-unmockable-opt-in-mocking-of-final-classesmethods

    String minioEndpoint =
        "http://" + MINIO_CONTAINER.getIp() + ":" + MINIO_CONTAINER.getExternalPort();

    MinioClient minioClient =
        new MinioClient(
            minioEndpoint, DockerComposeIT.MINIO_ACCESS_KEY, DockerComposeIT.MINIO_SECRET_KEY);

    this.minioClient = new BenchFlowExperimentMinioClient(minioClient);

    this.minioClient.initializeBuckets();

    experimentDefinitionInputStream = TestFiles.getValidExperimentDefinitionInputStream();
  }

  @Test
  public void saveGetRemoveBenchFlowExperimentDefinition() throws Exception {

    minioClient.saveExperimentDefinition(VALID_EXPERIMENT_ID, experimentDefinitionInputStream);

    InputStream receivedInputStream = minioClient.getExperimentDefinition(VALID_EXPERIMENT_ID);

    Assert.assertNotNull(receivedInputStream);

    String receivedString =
        IOUtils.toString(
            new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)),
            StandardCharsets.UTF_8);

    Assert.assertEquals(TestFiles.getValidExperimentDefinitionString(), receivedString);

    minioClient.removeExperimentDefinition(VALID_EXPERIMENT_ID);

    receivedInputStream = minioClient.getExperimentDefinition(VALID_EXPERIMENT_ID);

    Assert.assertNull(receivedInputStream);
  }
}
