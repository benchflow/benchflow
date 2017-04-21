package cloud.benchflow.testmanager.resources;

import cloud.benchflow.faban.client.responses.RunStatus;
import cloud.benchflow.testmanager.api.request.ChangeBenchFlowTestStateRequest;
import cloud.benchflow.testmanager.api.response.ChangeBenchFlowTestStateResponse;
import cloud.benchflow.testmanager.api.response.RunBenchFlowTestResponse;
import cloud.benchflow.testmanager.archive.TestArchives;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.helpers.TestConstants;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutorService;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 26.02.17.
 */
public class BenchFlowTestEndpointTest {

    private static ExecutorService executorServiceMock = Mockito.mock(ExecutorService.class);
    private static MinioService minioServiceMock = Mockito.mock(MinioService.class);
    private static BenchFlowTestModelDAO testModelDAOMock = Mockito.mock(BenchFlowTestModelDAO.class);
    private static BenchFlowExperimentModelDAO experimentModelDAOMock = Mockito.mock(
            BenchFlowExperimentModelDAO.class);
    private static UserDAO userDAOMock = Mockito.mock(UserDAO.class);
    private static BenchFlowExperimentManagerService experimentManagerServiceMock = Mockito.mock(
            BenchFlowExperimentManagerService.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(MultiPartFeature.class)
            .addResource(new BenchFlowTestResource(executorServiceMock, minioServiceMock, testModelDAOMock, experimentModelDAOMock, userDAOMock, experimentManagerServiceMock))
            .build();

    @Test
    public void runValidBenchFlowTest() throws Exception {

        String benchFlowTestName = "testNameExample";
        User user = BenchFlowConstants.BENCHFLOW_USER;

        Mockito.doReturn(
                user.getUsername() + BenchFlowConstants.MODEL_ID_DELIMITER + benchFlowTestName + BenchFlowConstants.MODEL_ID_DELIMITER + 1).when(
                testModelDAOMock).addTestModel(benchFlowTestName, user);

        FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
                TestArchives.getValidTestArchiveFile(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        multiPart.bodyPart(fileDataBodyPart);

        Response response = resources.client()
                .target(BenchFlowConstants.getPathFromUsername(user.getUsername()))
                .path(BenchFlowConstants.TESTS_PATH)
                .path(BenchFlowTestResource.RUN_PATH)
                .register(MultiPartFeature.class)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(multiPart, multiPart.getMediaType()));

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        RunBenchFlowTestResponse testResponse = response.readEntity(RunBenchFlowTestResponse.class);

        Assert.assertNotNull(testResponse);
        Assert.assertTrue(testResponse.getTestID().contains(benchFlowTestName));

    }

    @Test
    public void runInvalidArchiveBenchFlowTest() throws Exception {

        // TODO

    }

    @Test
    public void changeBenchFlowTestState() throws Exception {

        BenchFlowTestModel.BenchFlowTestState state = BenchFlowTestModel.BenchFlowTestState.COMPLETED;
        String testID = TestConstants.VALID_TEST_ID;

        Mockito.doReturn(state).when(testModelDAOMock).setTestState(testID, state);

        ChangeBenchFlowTestStateRequest stateRequest = new ChangeBenchFlowTestStateRequest(state);

        Response response = resources.client()
                .target(BenchFlowConstants.getPathFromTestID(testID))
                .path(BenchFlowTestResource.STATE_PATH)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(stateRequest, MediaType.APPLICATION_JSON));

        ChangeBenchFlowTestStateResponse stateResponse = response.readEntity(ChangeBenchFlowTestStateResponse.class);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertNotNull(stateResponse);
        Assert.assertEquals(state, stateResponse.getState());

    }

    @Test
    public void invalidBenchFlowTestState() throws Exception {

        // TODO

    }

    @Test
    public void getBenchFlowTestStatus() throws Exception {

        String testID = TestConstants.VALID_TEST_ID;

        BenchFlowTestModel testModel = new BenchFlowTestModel(TestConstants.TEST_USER, TestConstants.VALID_BENCHFLOW_TEST_NAME, TestConstants.VALID_TEST_NUMBER);
        testModel.setState(BenchFlowTestModel.BenchFlowTestState.RUNNING);
        BenchFlowExperimentModel experimentModel = new BenchFlowExperimentModel(testModel.getId(), 1);
        testModel.addExperimentModel(experimentModel);
        experimentModel.setTrialStatus(1, RunStatus.Code.COMPLETED);
        testModel.addExperimentModel(experimentModel);

        Mockito.doReturn(testModel)
                .when(testModelDAOMock)
                .getTestModel(testID);

        Response response = resources.client()
                .target(BenchFlowConstants.getPathFromTestID(testID))
                .path(BenchFlowTestResource.STATUS_PATH)
                .request(MediaType.APPLICATION_JSON)
                .get();

        BenchFlowTestModel statusResponse = response.readEntity(BenchFlowTestModel.class);


        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertNotNull(statusResponse);
        // TODO - adjust when status object is decided
        Assert.assertEquals(testID, statusResponse.getId());

    }
}