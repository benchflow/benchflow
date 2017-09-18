package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.DockerComposeIT;
import cloud.benchflow.testmanager.api.response.GetUserTestsResponse;
import cloud.benchflow.testmanager.configurations.BenchFlowTestManagerConfiguration;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.UserIDAlreadyExistsException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-09-18
 */
public class BenchFlowTestResourceIT extends DockerComposeIT {

  @Rule
  public final DropwizardAppRule<BenchFlowTestManagerConfiguration> RULE =
      new DropwizardAppRule<>(BenchFlowTestManagerApplication.class, "../configuration.yml",
          ConfigOverride.config("mongoDB.hostname", MONGO_CONTAINER.getIp()),
          ConfigOverride.config("mongoDB.port", String.valueOf(MONGO_CONTAINER.getExternalPort())),
          ConfigOverride.config("minio.address",
              "http://" + MINIO_CONTAINER.getIp() + ":" + MINIO_CONTAINER.getExternalPort()),
          ConfigOverride.config("minio.accessKey", MINIO_ACCESS_KEY),
          ConfigOverride.config("minio.secretKey", MINIO_SECRET_KEY),
          ConfigOverride.config("benchFlowExperimentManager.address", "localhost"));

  private UserDAO userDAO;
  private BenchFlowTestModelDAO testModelDAO;

  @Before
  public void setUp() throws Exception {

    userDAO = BenchFlowTestManagerApplication.getUserDAO();
    testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();

  }

  @Test
  public void getUserTests() throws Exception {

    // ############### save user + tests in DB ########################
    String username = "testUser";

    User user = userDAO.addUser(username);

    List<String> testNames = Arrays.asList("testA", "testB", "testC");

    testNames.forEach(testName -> testModelDAO.addTestModel(testName, user));

    // ############### query API ######################################
    JerseyClientConfiguration configuration = new JerseyClientConfiguration();
    configuration.setTimeout(Duration.milliseconds(1000));

    Client client =
        new JerseyClientBuilder(RULE.getEnvironment()).using(configuration).build("test client");

    String target = "http://localhost:" + RULE.getLocalPort();

    Response response = client.target(target).path(BenchFlowConstants.getPathFromUsername(username))
        .path(BenchFlowTestResource.TEST_PATH).request().get();

    // ############### assert all tests retrieved #####################
    List<String> testIDs = response.readEntity(GetUserTestsResponse.class).getTestIDs();

    Assert.assertEquals(testNames.size(), testIDs.size());

    testNames.stream().map(name -> BenchFlowConstants.getTestID(username, name, 1))
        .forEach(id -> Assert.assertTrue(testIDs.contains(id)));

  }

  @Test
  public void getUserTestsInvalidUser() throws Exception {

    String username = "testUser";

    JerseyClientConfiguration configuration = new JerseyClientConfiguration();
    configuration.setTimeout(Duration.milliseconds(1000));

    Client client =
        new JerseyClientBuilder(RULE.getEnvironment()).using(configuration).build("test client");

    String target = "http://localhost:" + RULE.getLocalPort();

    Response response = client.target(target).path(BenchFlowConstants.getPathFromUsername(username))
        .path(BenchFlowTestResource.TEST_PATH).request().get();

    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

  }
}
