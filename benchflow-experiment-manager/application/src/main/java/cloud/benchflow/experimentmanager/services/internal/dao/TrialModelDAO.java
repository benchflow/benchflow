package cloud.benchflow.experimentmanager.services.internal.dao;

import cloud.benchflow.experimentmanager.exceptions.TrialIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.TrialModel;
import cloud.benchflow.experimentmanager.models.TrialModel.HandleTrialResultState;
import cloud.benchflow.faban.client.responses.RunStatus;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-07
 */
public class TrialModelDAO extends AbstractDAO {

  private static Logger logger = LoggerFactory.getLogger(TrialModelDAO.class.getSimpleName());

  public TrialModelDAO(MongoClient mongoClient) {
    super(mongoClient);
  }

  public synchronized void setTrialModelAsStarted(String trialID)
      throws TrialIDDoesNotExistException {

    logger.info("setTrialModelAsStarted: " + trialID);

    setTrialStatus(trialID, RunStatus.Code.STARTED);
  }

  public synchronized void setFabanTrialID(String trialID, String fabanRunID) {

    logger.info("setFabanTrialID: " + trialID + " with " + fabanRunID);

    TrialModel trialModel = getTrialModel(trialID);

    if (trialModel != null) {
      trialModel.setFabanRunID(fabanRunID);
      datastore.save(trialModel);
    }
  }

  public synchronized void setHandleTrialResultState(String trialID, HandleTrialResultState state) {

    logger.info("setHandleTrialResultState: " + trialID + " with " + state.name());

    TrialModel trialModel = getTrialModel(trialID);

    if (trialModel != null) {
      trialModel.setHandleTrialResultState(state);
      datastore.save(trialModel);
    }

  }

  public synchronized HandleTrialResultState getHandleTrialResultState(String trialID) {

    logger.info("getHandleTrialResultState: " + trialID);

    TrialModel trialModel = getTrialModel(trialID);

    return trialModel.getHandleTrialResultState();

  }

  public synchronized void setTrialStatus(String trialID, RunStatus.Code statusCode) {

    logger.info("setTrialStatus: " + trialID + " with " + statusCode.name());

    TrialModel trialModel = getTrialModel(trialID);

    if (trialModel != null) {
      trialModel.setStatus(statusCode);
      datastore.save(trialModel);
    }
  }

  public synchronized RunStatus.Code getTrialStatus(String trialID) {

    logger.info("getTrialStatus: " + trialID);

    TrialModel trialModel = getTrialModel(trialID);

    return trialModel.getStatus();
  }

  public synchronized void incrementRetries(String trialID) {
    logger.info("incrementRetries: " + trialID);

    TrialModel trialModel = getTrialModel(trialID);

    if (trialModel != null) {
      trialModel.incrementRetries();
      datastore.save(trialModel);
    }

  }

  public synchronized int getNumRetries(String trialID) {

    logger.info("getNumRetries: " + trialID);

    TrialModel trialModel = getTrialModel(trialID);

    return trialModel.getNumRetries();
  }

  private synchronized TrialModel getTrialModel(String trialID) {

    logger.info("getTrialModel: " + trialID);

    return datastore.createQuery(TrialModel.class).field(TrialModel.ID_FIELD_NAME).equal(trialID)
        .get();
  }

}
