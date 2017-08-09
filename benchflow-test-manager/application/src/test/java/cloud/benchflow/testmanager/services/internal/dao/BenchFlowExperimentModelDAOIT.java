package cloud.benchflow.testmanager.services.internal.dao;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;
import static cloud.benchflow.testmanager.helpers.constants.TestConstants.LOAD_TEST_NAME;
import static org.junit.Assert.assertEquals;

import cloud.benchflow.faban.client.responses.RunStatus;
import cloud.benchflow.testmanager.DockerComposeIT;
import cloud.benchflow.testmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.testmanager.helpers.constants.TestConstants;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.User;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 22.02.17.
 */
public class BenchFlowExperimentModelDAOIT extends DockerComposeIT {

  @Rule
  public ExpectedException exception = ExpectedException.none();
  private BenchFlowTestModelDAO testModelDAO;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private UserDAO userDAO;
  private User testUser;
  private String testID;

  @Before
  public void setUp() throws Exception {

    testModelDAO = new BenchFlowTestModelDAO(mongoClient);

    experimentModelDAO = new BenchFlowExperimentModelDAO(mongoClient, testModelDAO);

    userDAO = new UserDAO(mongoClient, testModelDAO);

    testUser = userDAO.addUser(TestConstants.TEST_USER_NAME);

    testID = testModelDAO.addTestModel(LOAD_TEST_NAME, testUser);

    BenchFlowTestModel model = testModelDAO.getTestModel(testID);

    Assert.assertNotNull(model);
    assertEquals(testID, model.getId());
  }

  @After
  public void tearDown() throws Exception {

    userDAO.removeUser(testUser.getUsername());
  }

  @Test
  public void addTrialStatus() throws Exception {

    int trialNumber = 0;

    String experimentID = experimentModelDAO.addExperiment(testID);

    // STARTED
    experimentModelDAO.addTrialStatus(experimentID, trialNumber, RunStatus.Code.STARTED);
    RunStatus.Code trialStatus = experimentModelDAO.getTrialStatus(experimentID, trialNumber);

    Assert.assertNotNull(trialStatus);
    assertEquals(RunStatus.Code.STARTED, trialStatus);

    // TERMINATED
    experimentModelDAO.addTrialStatus(experimentID, trialNumber, RunStatus.Code.COMPLETED);
    trialStatus = experimentModelDAO.getTrialStatus(experimentID, trialNumber);

    Assert.assertNotNull(trialStatus);
    assertEquals(RunStatus.Code.COMPLETED, trialStatus);
  }

  @Test
  public void addMultipleExperiments() throws Exception {

    // make sure that the experiment counter is incremented correctly

    String firstID = experimentModelDAO.addExperiment(testID);

    assertEquals(testID + MODEL_ID_DELIMITER + 1, firstID);

    BenchFlowTestModel testModel = testModelDAO.getTestModel(testID);

    assertEquals(1, testModel.getExperimentModels().size());

    String secondID = experimentModelDAO.addExperiment(testID);

    assertEquals(testID + MODEL_ID_DELIMITER + 2, secondID);

    testModel = testModelDAO.getTestModel(testID);

    assertEquals(2, testModel.getExperimentModels().size());
  }

  @Test
  public void addTrialToMissingExperiment() throws Exception {

    exception.expect(BenchFlowExperimentIDDoesNotExistException.class);

    experimentModelDAO.addTrialStatus("not_valid", 1, RunStatus.Code.COMPLETED);
  }

  @Test
  public void testHashedID() throws Exception {

    experimentModelDAO.addExperiment(testID);

    DBCollection collection =
        testModelDAO.getDatastore().getCollection(BenchFlowExperimentModel.class);

    collection.getIndexInfo().forEach(dbObject -> {
      BasicDBObject index = (BasicDBObject) dbObject;
      if (!index.getString("name").equals("_id_")) {
        assertEquals("hashed",
            ((DBObject) index.get("key")).get(BenchFlowExperimentModel.HASHED_ID_FIELD_NAME));
      }
    });
  }
}
