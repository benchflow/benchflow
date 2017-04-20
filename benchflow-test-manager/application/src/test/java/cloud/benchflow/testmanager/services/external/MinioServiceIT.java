package cloud.benchflow.testmanager.services.external;

import cloud.benchflow.testmanager.archive.TestArchives;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.DockerComposeIT;
import io.minio.MinioClient;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static cloud.benchflow.testmanager.helpers.TestConstants.VALID_EXPERIMENT_ID;
import static cloud.benchflow.testmanager.helpers.TestConstants.VALID_TEST_ID;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 16.02.17.
 */
public class MinioServiceIT extends DockerComposeIT {

    private MinioService minioService;

    private InputStream ptDefinitionInputStream;

    private InputStream deploymentDescriptorInputStream;
    private Map<String, InputStream> bpmnModelsMap;

    @Before
    public void setUp() throws Exception {

        // TODO - see how to mock final class MinioClient
        // https://github.com/mockito/mockito/wiki/What%27s-new-in-Mockito-2#mock-the-unmockable-opt-in-mocking-of-final-classesmethods

        String minioEndpoint = "http://" + MINIO_CONTAINER.getIp() + ":" + MINIO_CONTAINER.getExternalPort();

        MinioClient minioClient = new MinioClient(minioEndpoint, DockerComposeIT.MINIO_ACCESS_KEY, DockerComposeIT.MINIO_SECRET_KEY);

        if (!minioClient.bucketExists(BenchFlowConstants.TESTS_BUCKET))
            minioClient.makeBucket(BenchFlowConstants.TESTS_BUCKET);

        minioService = new MinioService(minioClient);

        ptDefinitionInputStream = TestArchives.getValidPTDefinitionInputStream();

        deploymentDescriptorInputStream = TestArchives.getValidDeploymentDescriptorInputStream();

        bpmnModelsMap = TestArchives.getValidBPMNModels();

    }

    @Test
    public void saveGetRemoveBenchFlowTestDefinition() throws Exception {

        minioService.saveTestDefinition(VALID_TEST_ID, ptDefinitionInputStream);

        InputStream receivedInputStream = minioService.getTestDefinition(VALID_TEST_ID);

        Assert.assertNotNull(receivedInputStream);

        String receivedString = IOUtils.toString(new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)), StandardCharsets.UTF_8);

        Assert.assertEquals(TestArchives.getValidPTDefinitionString(), receivedString);

        minioService.removeTestDefinition(VALID_TEST_ID);

        receivedInputStream = minioService.getTestDefinition(VALID_TEST_ID);

        Assert.assertNull(receivedInputStream);

    }

    @Test
    public void saveGetRemoveDeploymentDescriptor() throws Exception {

        minioService.saveTestDeploymentDescriptor(VALID_TEST_ID, deploymentDescriptorInputStream);

        InputStream receivedInputStream = minioService.getTestDeploymentDescriptor(VALID_TEST_ID);

        Assert.assertNotNull(receivedInputStream);

        String receivedString = IOUtils.toString(new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)), StandardCharsets.UTF_8);

        Assert.assertEquals(TestArchives.getValidDeploymentDescriptorString(), receivedString);

        minioService.removeTestDeploymentDescriptor(VALID_TEST_ID);

        receivedInputStream = minioService.getTestDeploymentDescriptor(VALID_TEST_ID);

        Assert.assertNull(receivedInputStream);

    }


    @Test
    public void saveGetRemoveBPMNDefinitions() throws Exception {

        // TODO

        bpmnModelsMap.forEach((name, model) -> {

            minioService.saveTestBPMNModel(VALID_TEST_ID, name, model);

            InputStream receivedInputStream = minioService.getTestBPMNModel(VALID_TEST_ID, name);

            Assert.assertNotNull(receivedInputStream);

            // TODO - assert the content is the same

//            String receivedString = IOUtils.toString(new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)), StandardCharsets.UTF_8);
//
//            Assert.assertEquals(TestArchives.getValidDeploymentDescriptorString(), receivedString);

            minioService.removeTestBPMNModel(VALID_TEST_ID, name);

            receivedInputStream = minioService.getTestBPMNModel(VALID_TEST_ID, name);

            Assert.assertNull(receivedInputStream);

        });

    }

    @Test
    public void saveGetRemoveBenchFlowExperimentDefinition() throws Exception {

        minioService.saveExperimentDefinition(VALID_EXPERIMENT_ID, ptDefinitionInputStream);

        InputStream receivedInputStream = minioService.getExperimentDefinition(VALID_EXPERIMENT_ID);

        Assert.assertNotNull(receivedInputStream);

        String receivedString = IOUtils.toString(new ByteArrayInputStream(IOUtils.toByteArray(receivedInputStream)), StandardCharsets.UTF_8);

        Assert.assertEquals(TestArchives.getValidPTDefinitionString(), receivedString);

        minioService.removeExperimentDefinition(VALID_EXPERIMENT_ID);

        receivedInputStream = minioService.getExperimentDefinition(VALID_EXPERIMENT_ID);

        Assert.assertNull(receivedInputStream);

    }

}