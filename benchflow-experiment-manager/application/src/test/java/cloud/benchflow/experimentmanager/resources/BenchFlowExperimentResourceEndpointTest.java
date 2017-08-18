package cloud.benchflow.experimentmanager.resources;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.helpers.data.BenchFlowData;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.TrialModel;
import cloud.benchflow.experimentmanager.scheduler.ExperimentTaskScheduler;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.responses.RunStatus.StatusCode;
import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-06-14
 */
public class BenchFlowExperimentResourceEndpointTest {

  private static MinioService minioServiceMock = Mockito.mock(MinioService.class);
  private static BenchFlowExperimentModelDAO experimentModelDAOMock =
      Mockito.mock(BenchFlowExperimentModelDAO.class);
  private static ExperimentTaskScheduler experimentTaskScheduler =
      Mockito.mock(ExperimentTaskScheduler.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder().addProvider(MultiPartFeature.class)
          .addResource(new BenchFlowExperimentResource(minioServiceMock, experimentModelDAOMock,
              experimentTaskScheduler))
          .build();

  @Test
  public void getExperimentStatus() throws Exception {

    String experimentID = BenchFlowData.VALID_EXPERIMENT_ID_2_TRIAL;

    BenchFlowExperimentModel experimentModel = new BenchFlowExperimentModel(experimentID);
    experimentModel.setNumTrials(2);

    experimentModel.setState(BenchFlowExperimentState.RUNNING);

    TrialModel trial1 = new TrialModel(experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + 1);
    trial1.setFabanStatus(StatusCode.COMPLETED);
    String fabanRunID1 = "FabanRunID1";
    trial1.setFabanRunID(fabanRunID1);

    experimentModel.addTrial(0, trial1);

    TrialModel trial2 = new TrialModel(experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + 2);
    trial2.setFabanStatus(StatusCode.COMPLETED);
    String fabanRunID2 = "FabanRunID2";
    trial2.setFabanRunID(fabanRunID2);

    experimentModel.addTrial(1, trial2);

    Mockito.doReturn(experimentModel).when(experimentModelDAOMock).getExperimentModel(experimentID);

    Response response = resources.client()
        .target(BenchFlowConstants.getPathFromExperimentID(experimentID))
        .path(BenchFlowExperimentResource.STATUS_PATH).request(MediaType.APPLICATION_JSON).get();

    BenchFlowExperimentModel receivedModel = response.readEntity(BenchFlowExperimentModel.class);

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Assert.assertNotNull(receivedModel);

    Assert.assertEquals(experimentID, receivedModel.getId());
    Assert.assertTrue(receivedModel.getDriverMakerExperimentBundle()
        .contains("/minio/" + BenchFlowConstants.TESTS_BUCKET));
    Assert.assertTrue(receivedModel.getTrials().get(0L).getFabanRunStatus().contains(fabanRunID1));
    Assert.assertTrue(receivedModel.getTrials().get(1L).getFabanRunStatus().contains(fabanRunID2));


  }

}
