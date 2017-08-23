package cloud.benchflow.testmanager.resources;

import cloud.benchflow.faban.client.responses.RunStatus;
import cloud.benchflow.testmanager.api.request.SubmitTrialStatusRequest;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.helpers.constants.TestConstants;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
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
public class BenchFlowTrialEndpointTest {

  private static BenchFlowExperimentModelDAO experimentModelDAOMock =
      Mockito.mock(BenchFlowExperimentModelDAO.class);

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new BenchFlowTrialResource(experimentModelDAOMock)).build();

  @Test
  public void submitTrialStatus() throws Exception {

    String trialID = TestConstants.LOAD_TRIAL_ID;

    SubmitTrialStatusRequest statusRequest =
        new SubmitTrialStatusRequest(RunStatus.StatusCode.COMPLETED);

    Response response = resources.client().target(BenchFlowConstants.getPathFromTrialID(trialID))
        .path(BenchFlowTrialResource.STATUS_PATH).request()
        .put(Entity.entity(statusRequest, MediaType.APPLICATION_JSON));

    Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
  }
}
