package cloud.benchflow.experimentmanager.services.internal.dao;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.getTrialID;
import static cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.ID_FIELD_NAME;

import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.FailureStatus;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.experimentmanager.models.TrialModel;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 06.03.17.
 */
public class BenchFlowExperimentModelDAO extends AbstractDAO {

  private static Logger logger =
      LoggerFactory.getLogger(BenchFlowExperimentModelDAO.class.getSimpleName());

  public BenchFlowExperimentModelDAO(MongoClient mongoClient) {
    super(mongoClient);
  }

  public synchronized void addExperiment(String experimentID) {

    logger.info("addExperiment: " + experimentID);

    BenchFlowExperimentModel experimentModel = new BenchFlowExperimentModel(experimentID);

    datastore.save(experimentModel);
  }

  public synchronized BenchFlowExperimentModel getExperimentModel(String experimentID) {

    logger.info("getExperimentModel: " + experimentID);

    return datastore.createQuery(BenchFlowExperimentModel.class).field(ID_FIELD_NAME)
        .equal(experimentID).get();
  }

  public synchronized boolean experimentExists(String experimentID) {

    logger.info("getExperimentState: " + experimentID);

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    return experimentModel != null;

  }

  public synchronized BenchFlowExperimentState getExperimentState(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("getExperimentState: " + experimentID);

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      throw new BenchFlowExperimentIDDoesNotExistException();
    }

    return experimentModel.getState();
  }

  public synchronized BenchFlowExperimentState setExperimentState(String experimentID,
      BenchFlowExperimentState state) {

    logger.info("setExperimentState: " + experimentID + " to " + state.name());

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      return null;
    }

    experimentModel.setState(state);

    datastore.save(experimentModel);

    return experimentModel.getState();
  }



  public synchronized RunningState getRunningState(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("getRunningState: " + experimentID);

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      throw new BenchFlowExperimentIDDoesNotExistException();
    }

    return experimentModel.getRunningState();
  }

  public synchronized BenchFlowExperimentState setRunningState(String experimentID,
      RunningState state) {

    logger.info("setRunningState: " + experimentID + " state: " + state.name());

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      return null;
    }

    experimentModel.setRunningState(state);

    datastore.save(experimentModel);

    return experimentModel.getState();
  }

  public synchronized BenchFlowExperimentModel.TerminatedState getTerminatedState(
      String experimentID) throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("getTerminatedState: " + experimentID);

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      throw new BenchFlowExperimentIDDoesNotExistException();
    }

    return experimentModel.getTerminatedState();
  }

  public synchronized BenchFlowExperimentModel.TerminatedState setTerminatedState(
      String experimentID, BenchFlowExperimentModel.TerminatedState state) {

    logger.info("setTerminatedState: " + experimentID + " state: " + state.name());

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      return null;
    }

    experimentModel.setTerminatedState(state);

    datastore.save(experimentModel);

    return experimentModel.getTerminatedState();
  }

  public synchronized void setFailureStatus(String experimentID, FailureStatus failureStatus) {

    logger.info("setFailureStatus: " + experimentID + " state: " + failureStatus.name());

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      // should not happen
      return;
    }

    experimentModel.setFailureStatus(failureStatus);

    datastore.save(experimentModel);

  }

  public synchronized FailureStatus getFailureStatus(String experimentID) {

    logger.info("getFailureStatus: " + experimentID);

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      // should not happen
      return null;
    }

    return experimentModel.getFailureStatus();

  }

  public synchronized int getNumTrialRetries(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("getNumTrialRetries: " + experimentID);

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      throw new BenchFlowExperimentIDDoesNotExistException();
    }

    return experimentModel.getNumTrialRetries();
  }

  public synchronized String addTrial(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("addTrial: " + experimentID);

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      throw new BenchFlowExperimentIDDoesNotExistException();
    }

    // increment to next trial number
    long trialNumber = experimentModel.getNumExecutedTrials() + 1;

    String trialID = getTrialID(experimentID, trialNumber);

    // first save the Trial Model and then add it to Experiment Model
    TrialModel trialModel = new TrialModel(trialID);
    datastore.save(trialModel);

    experimentModel.addTrial(trialNumber, trialModel);

    datastore.save(experimentModel);

    return trialID;
  }

  public synchronized long getNumExecutedTrials(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("getNumExecutedTrials: " + experimentID);

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      throw new BenchFlowExperimentIDDoesNotExistException();
    }

    return experimentModel.getNumExecutedTrials();
  }

  public synchronized int getNumTrials(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("getNumTrials: " + experimentID);

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      throw new BenchFlowExperimentIDDoesNotExistException();
    }

    return experimentModel.getNumTrials();
  }

  public synchronized void setNumTrials(String experimentID, int numTrials)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("setNumTrials: " + experimentID);

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      throw new BenchFlowExperimentIDDoesNotExistException();
    }

    experimentModel.setNumTrials(numTrials);

    datastore.save(experimentModel);
  }

  public synchronized String getLastExecutedTrialID(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    logger.info("getLastExecutedTrialID: " + experimentID);

    BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

    if (experimentModel == null) {
      throw new BenchFlowExperimentIDDoesNotExistException();
    }

    return experimentModel.getLastExecutedTrialID();
  }
}
