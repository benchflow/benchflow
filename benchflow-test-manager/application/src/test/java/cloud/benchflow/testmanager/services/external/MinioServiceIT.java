package cloud.benchflow.testmanager.services.external;

import static cloud.benchflow.testmanager.helpers.constants.TestConstants.LOAD_EXPERIMENT_ID;
import static cloud.benchflow.testmanager.helpers.constants.TestConstants.LOAD_TEST_ID;

import cloud.benchflow.testmanager.DockerComposeIT;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.helpers.constants.TestBundle;
import io.minio.MinioClient;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 16.02.17.
 */
public class MinioServiceIT extends DockerComposeIT {

  // needs to be subfolder of current folder for Wercker
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("target"));

  private MinioService minioService;

  private InputStream ptDefinitionInputStream;

  private InputStream deploymentDescriptorInputStream;
  private Map<String, InputStream> bpmnModelsMap;

  @Before
  public void setUp() throws Exception {

    // TODO - see how to mock final class MinioClient
    // https://github.com/mockito/mockito/wiki/What%27s-new-in-Mockito-2#mock-the-unmockable-opt-in-mocking-of-final-classesmethods

    String minioEndpoint =
        "http://" + MINIO_CONTAINER.getIp() + ":" + MINIO_CONTAINER.getExternalPort();

    MinioClient minioClient = new MinioClient(minioEndpoint, DockerComposeIT.MINIO_ACCESS_KEY,
        DockerComposeIT.MINIO_SECRET_KEY);

    if (!minioClient.bucketExists(BenchFlowConstants.TESTS_BUCKET)) {
      minioClient.makeBucket(BenchFlowConstants.TESTS_BUCKET);
    }

    minioService = new MinioService(minioClient, 3);

    ptDefinitionInputStream = TestBundle.getValidTestDefinitionInputStream();

    deploymentDescriptorInputStream = TestBundle.getValidDeploymentDescriptorInputStream();

    bpmnModelsMap = TestBundle.getValidBPMNModels();
  }

  @Test
  public void saveGetRemoveBenchFlowTestDefinition() throws Exception {

    minioService.saveTestDefinition(LOAD_TEST_ID, ptDefinitionInputStream);

    InputStream receivedInputStream = minioService.getTestDefinition(LOAD_TEST_ID);

    Assert.assertNotNull(receivedInputStream);

    String receivedString = IOUtils.toString(
        new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)), StandardCharsets.UTF_8);

    Assert.assertEquals(TestBundle.getValidTestDefinitionString(), receivedString);

    minioService.removeTestDefinition(LOAD_TEST_ID);

    receivedInputStream = minioService.getTestDefinition(LOAD_TEST_ID);

    Assert.assertNull(receivedInputStream);
  }

  @Test
  public void saveGetRemoveDeploymentDescriptor() throws Exception {

    minioService.saveTestDeploymentDescriptor(LOAD_TEST_ID, deploymentDescriptorInputStream);

    InputStream receivedInputStream = minioService.getTestDeploymentDescriptor(LOAD_TEST_ID);

    Assert.assertNotNull(receivedInputStream);

    String receivedString = IOUtils.toString(
        new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)), StandardCharsets.UTF_8);

    Assert.assertEquals(TestBundle.getValidDeploymentDescriptorString(), receivedString);

    minioService.removeTestDeploymentDescriptor(LOAD_TEST_ID);

    receivedInputStream = minioService.getTestDeploymentDescriptor(LOAD_TEST_ID);

    Assert.assertNull(receivedInputStream);
  }

  @Test
  public void saveGetRemoveBPMNDefinitions() throws Exception {

    // TODO

    bpmnModelsMap.forEach((name, model) -> {
      minioService.saveTestBPMNModel(LOAD_TEST_ID, name, model);

      InputStream receivedInputStream = minioService.getTestBPMNModel(LOAD_TEST_ID, name);

      Assert.assertNotNull(receivedInputStream);

      // TODO - assert the content is the same

      //            String receivedString = IOUtils.toString(new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)), StandardCharsets.UTF_8);
      //
      //            Assert.assertEquals(TestBundle.getValidDeploymentDescriptorString(), receivedString);

      minioService.removeTestBPMNModel(LOAD_TEST_ID, name);

      receivedInputStream = minioService.getTestBPMNModel(LOAD_TEST_ID, name);

      Assert.assertNull(receivedInputStream);
    });
  }

  @Test
  public void saveGetRemoveBenchFlowExperimentDefinition() throws Exception {

    minioService.saveExperimentDefinition(LOAD_EXPERIMENT_ID, ptDefinitionInputStream);

    InputStream receivedInputStream = minioService.getExperimentDefinition(LOAD_EXPERIMENT_ID);

    Assert.assertNotNull(receivedInputStream);

    String receivedString = IOUtils.toString(
        new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)), StandardCharsets.UTF_8);

    Assert.assertEquals(TestBundle.getValidTestDefinitionString(), receivedString);

    minioService.removeExperimentDefinition(LOAD_EXPERIMENT_ID);

    receivedInputStream = minioService.getExperimentDefinition(LOAD_EXPERIMENT_ID);

    Assert.assertNull(receivedInputStream);
  }
}
