package cloud.benchflow.minioclient;

import cloud.benchflow.minioclient.helpers.TestFiles;
import io.minio.MinioClient;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static cloud.benchflow.minioclient.helpers.TestConstants.VALID_TEST_ID;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public class BenchFlowTestMinioClientIT extends DockerComposeIT {

  private BenchFlowTestMinioClient minioClient;

  private InputStream testDefinitionInputStream;

  private InputStream deploymentDescriptorInputStream;
  private Map<String, InputStream> bpmnModelsMap;

  @Before
  public void setUp() throws Exception {

    // TODO - see how to mock final class MinioClient
    // https://github.com/mockito/mockito/wiki/What%27s-new-in-Mockito-2#mock-the-unmockable-opt-in-mocking-of-final-classesmethods

    String minioEndpoint =
        "http://" + MINIO_CONTAINER.getIp() + ":" + MINIO_CONTAINER.getExternalPort();

    MinioClient minioClient =
        new MinioClient(
            minioEndpoint, DockerComposeIT.MINIO_ACCESS_KEY, DockerComposeIT.MINIO_SECRET_KEY);

    this.minioClient = new BenchFlowTestMinioClient(minioClient);

    this.minioClient.initializeBuckets();

    testDefinitionInputStream = TestFiles.getValidTestDefinitionInputStream();

    deploymentDescriptorInputStream = TestFiles.getValidDeploymentDescriptorInputStream();

    bpmnModelsMap = TestFiles.getValidBPMNModels();
  }

  @Test
  public void saveGetRemoveBenchFlowTestDefinition() throws Exception {

    minioClient.saveTestDefinition(VALID_TEST_ID, testDefinitionInputStream);

    InputStream receivedInputStream = minioClient.getTestDefinition(VALID_TEST_ID);

    Assert.assertNotNull(receivedInputStream);

    String receivedString =
        IOUtils.toString(
            new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)),
            StandardCharsets.UTF_8);

    Assert.assertEquals(TestFiles.getValidTestDefinitionString(), receivedString);

    minioClient.removeTestDefinition(VALID_TEST_ID);

    receivedInputStream = minioClient.getTestDefinition(VALID_TEST_ID);

    Assert.assertNull(receivedInputStream);
  }

  @Test
  public void saveGetRemoveDeploymentDescriptor() throws Exception {

    minioClient.saveTestDeploymentDescriptor(VALID_TEST_ID, deploymentDescriptorInputStream);

    InputStream receivedInputStream = minioClient.getTestDeploymentDescriptor(VALID_TEST_ID);

    Assert.assertNotNull(receivedInputStream);

    String receivedString =
        IOUtils.toString(
            new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)),
            StandardCharsets.UTF_8);

    Assert.assertEquals(TestFiles.getValidDeploymentDescriptorString(), receivedString);

    minioClient.removeTestDeploymentDescriptor(VALID_TEST_ID);

    receivedInputStream = minioClient.getTestDeploymentDescriptor(VALID_TEST_ID);

    Assert.assertNull(receivedInputStream);
  }

  @Test
  public void saveGetRemoveBPMNDefinitions() throws Exception {

    // TODO

    bpmnModelsMap.forEach(
        (name, model) -> {
          minioClient.saveTestBPMNModel(VALID_TEST_ID, name, model);

          InputStream receivedInputStream = minioClient.getTestBPMNModel(VALID_TEST_ID, name);

          Assert.assertNotNull(receivedInputStream);

          // TODO - assert the content is the same

          //            String receivedString = IOUtils.toString(new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)), StandardCharsets.UTF_8);
          //
          //            Assert.assertEquals(TestArchives.getValidDeploymentDescriptorString(), receivedString);

          minioClient.removeTestBPMNModel(VALID_TEST_ID, name);

          receivedInputStream = minioClient.getTestBPMNModel(VALID_TEST_ID, name);

          Assert.assertNull(receivedInputStream);
        });
  }
}
