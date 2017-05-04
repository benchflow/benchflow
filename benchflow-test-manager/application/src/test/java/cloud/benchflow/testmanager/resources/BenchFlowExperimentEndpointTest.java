package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.helpers.TestConstants;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentStatus;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.tasks.BenchFlowTestTaskController;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 27.02.17. */
public class BenchFlowExperimentEndpointTest {

  private static BenchFlowExperimentModelDAO experimentModelDAOMock =
      Mockito.mock(BenchFlowExperimentModelDAO.class);
  private static BenchFlowTestTaskController testTaskControllerMock =
      Mockito.mock(BenchFlowTestTaskController.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(
              new BenchFlowExperimentResource(experimentModelDAOMock, testTaskControllerMock))
          .build();

  @Test
  public void submitExperimentState() throws Exception {

    String experimentID = TestConstants.VALID_EXPERIMENT_ID;

    BenchFlowExperimentStateRequest request =
        new BenchFlowExperimentStateRequest(
            BenchFlowExperimentState.TERMINATED, BenchFlowExperimentStatus.COMPLETED);

    Response response =
        resources
            .client()
            .target(BenchFlowConstants.getPathFromExperimentID(experimentID))
            .path(BenchFlowExperimentResource.STATE_PATH)
            .request()
            .put(Entity.entity(request, MediaType.APPLICATION_JSON));

    Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
  }
}
