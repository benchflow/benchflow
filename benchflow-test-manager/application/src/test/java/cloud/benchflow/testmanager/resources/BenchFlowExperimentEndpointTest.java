package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.helpers.constants.TestConstants;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 27.02.17.
 */
public class BenchFlowExperimentEndpointTest {

  private static BenchFlowExperimentModelDAO experimentModelDAOMock =
      Mockito.mock(BenchFlowExperimentModelDAO.class);
  private static TestTaskScheduler testTaskControllerMock = Mockito.mock(TestTaskScheduler.class);
  private static BenchFlowTestModelDAO testModelDAOMock = Mockito.mock(BenchFlowTestModelDAO.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder().addResource(new BenchFlowExperimentResource(experimentModelDAOMock,
          testTaskControllerMock, testModelDAOMock)).build();

  @Test
  public void submitExperimentState() throws Exception {

    String experimentID = TestConstants.LOAD_EXPERIMENT_ID;

    Mockito.doReturn(BenchFlowTestModel.BenchFlowTestState.RUNNING).when(testModelDAOMock)
        .getTestState(BenchFlowConstants.getTestIDFromExperimentID(experimentID));

    BenchFlowExperimentStateRequest request = new BenchFlowExperimentStateRequest(
        BenchFlowExperimentState.TERMINATED, TerminatedState.COMPLETED);

    Response response =
        resources.client().target(BenchFlowConstants.getPathFromExperimentID(experimentID))
            .path(BenchFlowExperimentResource.STATE_PATH).request()
            .put(Entity.entity(request, MediaType.APPLICATION_JSON));

    Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
  }
}
