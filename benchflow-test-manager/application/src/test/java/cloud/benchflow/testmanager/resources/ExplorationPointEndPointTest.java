package cloud.benchflow.testmanager.resources;

import cloud.benchflow.dsl.ExplorationSpaceAPI;
import cloud.benchflow.dsl.explorationspace.JavaCompatExplorationSpaceConverter.JavaCompatExplorationSpace;
import cloud.benchflow.testmanager.api.response.ExplorationSpacePointResponse;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.helpers.constants.TestConstants;
import cloud.benchflow.testmanager.helpers.constants.TestFiles;
import cloud.benchflow.testmanager.models.explorationspace.MongoCompatibleExplorationSpace;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-05
 */
public class ExplorationPointEndPointTest {

  private static ExplorationModelDAO explorationModelDAOMock =
      Mockito.mock(ExplorationModelDAO.class);

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new ExplorationPointResource(explorationModelDAOMock)).build();

  @Test
  public void getValidExplorationSpacePointUsers() throws Exception {

    String testID = TestConstants.TEST_EXPLORATION_ONE_AT_A_TIME_USERS_ID;
    String testDefinitionString = TestFiles.getTestExplorationOneAtATimeUsersString();
    int explorationPointIndex = 0;

    JavaCompatExplorationSpace javaCompatExplorationSpace =
        ExplorationSpaceAPI.explorationSpaceFromTestYaml(testDefinitionString);

    MongoCompatibleExplorationSpace mongoCompatibleExplorationSpace =
        new MongoCompatibleExplorationSpace(javaCompatExplorationSpace);

    Mockito.doReturn(mongoCompatibleExplorationSpace).when(explorationModelDAOMock)
        .getExplorationSpace(testID);

    Response response = resources.client().target(BenchFlowConstants.getPathFromTestID(testID))
        .path(ExplorationPointResource.EXPLORATION_POINT_PATH)
        .path(String.valueOf(explorationPointIndex)).request().get();

    ExplorationSpacePointResponse pointResponse =
        response.readEntity(ExplorationSpacePointResponse.class);

    Assert.assertEquals(5, pointResponse.getUsers().intValue());

  }

  @Test
  public void getValidExplorationSpacePointUsersMemoryEnvironment() throws Exception {

    String testID = TestConstants.TEST_EXPLORATION_ONE_AT_A_TIME_USERS__MEMORY_ENVIRONMENT_ID;
    String testDefinitionString =
        TestFiles.getTestExplorationOneAtATimeUsersMemoryEnvironmentString();
    int explorationPointIndex = 0;

    JavaCompatExplorationSpace javaCompatExplorationSpace =
        ExplorationSpaceAPI.explorationSpaceFromTestYaml(testDefinitionString);

    MongoCompatibleExplorationSpace mongoCompatibleExplorationSpace =
        new MongoCompatibleExplorationSpace(javaCompatExplorationSpace);

    Mockito.doReturn(mongoCompatibleExplorationSpace).when(explorationModelDAOMock)
        .getExplorationSpace(testID);

    Response response = resources.client().target(BenchFlowConstants.getPathFromTestID(testID))
        .path(ExplorationPointResource.EXPLORATION_POINT_PATH)
        .path(String.valueOf(explorationPointIndex)).request().get();

    ExplorationSpacePointResponse pointResponse =
        response.readEntity(ExplorationSpacePointResponse.class);

    Assert.assertEquals(5, pointResponse.getUsers().intValue());

    Assert.assertEquals("500m", pointResponse.getMemory().get("camunda"));

    Assert.assertEquals("1",
        pointResponse.getEnvironment().get("camunda").get("SIZE_OF_THREADPOOL"));

  }
}
