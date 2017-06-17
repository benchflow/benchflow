package cloud.benchflow.testmanager.services.internal.dao;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;

import cloud.benchflow.faban.client.responses.RunStatus;
import cloud.benchflow.testmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import com.mongodb.MongoClient;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 22.02.17.
 */
public class BenchFlowExperimentModelDAO extends DAO {

  // TODO - this is also stored in the model?? read it directly from the model
  private static final String BENCHFLOW_EXPERIMENT_ID_FIELD_NAME = "id";

  private static Logger logger =
      LoggerFactory.getLogger(BenchFlowExperimentModelDAO.class.getSimpleName());

  private BenchFlowTestModelDAO testModelDAO;

  public BenchFlowExperimentModelDAO(MongoClient mongoClient, BenchFlowTestModelDAO testModelDAO) {
    super(mongoClient);
    this.testModelDAO = testModelDAO;
  }

  public synchronized String addExperiment(String testID)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("addExperiment: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    long experimentNumber = benchFlowTestModel.getNextExperimentNumber();

    BenchFlowExperimentModel experimentModel =
        new BenchFlowExperimentModel(testID, experimentNumber);

    // first save the Experiment Model and then add it to Test Model
    datastore.save(experimentModel);

    benchFlowTestModel.addExperimentModel(experimentModel);

    datastore.save(benchFlowTestModel);

    return experimentModel.getId();
  }

  private synchronized BenchFlowExperimentModel getExperiment(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("getExperiment: " + experimentID);

    final Query<BenchFlowExperimentModel> testModelQuery =
        datastore.createQuery(BenchFlowExperimentModel.class)
            .field(BENCHFLOW_EXPERIMENT_ID_FIELD_NAME).equal(experimentID);

    BenchFlowExperimentModel experimentModel = testModelQuery.get();

    if (experimentModel == null) {
      throw new BenchFlowExperimentIDDoesNotExistException();
    }

    return experimentModel;
  }

  public synchronized void setExplorationSpaceIndex(String experimentID, int index)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("setExplorationSpaceIndex: " + experimentID);

    final BenchFlowExperimentModel experimentModel;
    experimentModel = getExperiment(experimentID);

    experimentModel.setExplorationPointIndex(index);

    datastore.save(experimentModel);

  }

  public synchronized int getExplorationSpaceIndex(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("getExplorationSpaceIndex: " + experimentID);

    final BenchFlowExperimentModel experimentModel;
    experimentModel = getExperiment(experimentID);

    return experimentModel.getExplorationPointIndex();

  }

  public synchronized void setExperimentState(String experimentID, BenchFlowExperimentState state,
      RunningState runningState, TerminatedState terminatedState)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("setExperimentState: " + experimentID + " : " + state.name());

    final BenchFlowExperimentModel experimentModel;

    experimentModel = getExperiment(experimentID);

    experimentModel.setState(state);
    experimentModel.setRunningState(runningState);
    experimentModel.setTerminatedState(terminatedState);

    datastore.save(experimentModel);
  }

  public synchronized void addTrialStatus(String experimentID, long trialNUmber,
      RunStatus.Code status) throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("addTrialStatus: " + experimentID + MODEL_ID_DELIMITER + trialNUmber + " : "
        + status.name());

    final BenchFlowExperimentModel experimentModel;

    experimentModel = getExperiment(experimentID);

    experimentModel.setTrialStatus(trialNUmber, status);
    datastore.save(experimentModel);
  }

  public synchronized RunStatus.Code getTrialStatus(String experimentID, long trialNumber)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("getTrialStatus: " + experimentID + MODEL_ID_DELIMITER + trialNumber);

    final BenchFlowExperimentModel experimentModel = getExperiment(experimentID);

    return experimentModel.getTrialStatus(trialNumber);
  }
}
