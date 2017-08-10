package cloud.benchflow.testmanager;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;
import static cloud.benchflow.testmanager.constants.BenchFlowConstants.TESTS_PATH;
import static cloud.benchflow.testmanager.constants.BenchFlowConstants.getPathFromUsername;

import cloud.benchflow.testmanager.api.response.RunBenchFlowTestResponse;
import cloud.benchflow.testmanager.configurations.BenchFlowTestManagerConfiguration;
import cloud.benchflow.testmanager.helpers.constants.TestBundle;
import cloud.benchflow.testmanager.resources.BenchFlowTestResource;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import java.io.File;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-06
 */
public class TestManagerSmokeTestsIT extends DockerComposeIT {

  /**
   * Tests that ensure that given tests can be submitted successfully.
   * TODO - add integration with experiment-manager to check complete execution or
   * TODO - mocking of experiment-manager
   */

  private static String TEST_USERNAME = "smokeTestUser";

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

  // needs to be subfolder of current folder for Wercker
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("target"));

  private Client client;

  @Before
  public void setUp() throws Exception {
    // needed for multipart client
    // https://github.com/dropwizard/dropwizard/issues/1013
    JerseyClientConfiguration configuration = new JerseyClientConfiguration();
    configuration.setChunkedEncodingEnabled(false);
    // needed because parsing testYaml takes more than default time
    configuration.setTimeout(Duration.milliseconds(5000));

    client =
        new JerseyClientBuilder(RULE.getEnvironment()).using(configuration).build("test client");

  }

  private String getExpectedTestID(String testName) {
    return TEST_USERNAME + MODEL_ID_DELIMITER + testName + MODEL_ID_DELIMITER + 1;
  }

  @Test
  public void runLoadTest() throws Exception {

    String testName = "WfMSLoadTest";

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getLoadTestBundleFile(temporaryFolder), MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart);

  }

  @Test
  public void runExplorationExhaustiveOneAtATimeUsersTest() throws Exception {

    String testName = "WfMSExhaustiveExplorationOneAtATimeSelectionUsersTest";

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestExplorationOneAtATimeUsersBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart);

  }

  @Test
  public void runExplorationExhaustiveOneAtATimeMemoryTest() throws Exception {

    String testName = "WfMSExhaustiveExplorationOneAtATimeSelectionMemoryTest";

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestExplorationOneAtATimeMemoryBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart);

  }

  @Test
  public void runTestExplorationRandomUsersTest() throws Exception {

    String testName = "WfMSExhaustiveExplorationRandomSelectionUsersTest";

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestExplorationRandomUsersBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart);

  }

  @Test
  public void runTestExplorationExhaustiveOneAtATimeUsersEnvironmentTest() throws Exception {

    String testName = "WfMSExhaustiveExplorationOneAtATimeSelectionUsersEnvironmentTest";

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestExplorationOneAtATimeUsersEnvironmentBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart);


  }

  @Test
  public void runTestTerminationCriteria() throws Exception {

    String testName = "TestTerminationCriteriaTest";

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestTerminationCriteriaBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart);


  }

  @Test
  public void runTestStepUsers() throws Exception {

    String testName = "WfMSStepUsersExhaustiveExplorationTest";

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestStepUsersBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart);


  }

  private void runTest(String testName, FileDataBodyPart fileDataBodyPart) {

    String expectedTestID = getExpectedTestID(testName);

    MultiPart multiPart = new MultiPart();
    multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
    multiPart.bodyPart(fileDataBodyPart);

    Response response = client.target(String.format("http://localhost:%d/", RULE.getLocalPort()))
        .path(getPathFromUsername(TEST_USERNAME)).path(TESTS_PATH)
        .path(BenchFlowTestResource.RUN_PATH).register(MultiPartFeature.class)
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(multiPart, multiPart.getMediaType()));

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    RunBenchFlowTestResponse testResponse = response.readEntity(RunBenchFlowTestResponse.class);
    Assert.assertEquals(expectedTestID, testResponse.getTestID());

  }
}
