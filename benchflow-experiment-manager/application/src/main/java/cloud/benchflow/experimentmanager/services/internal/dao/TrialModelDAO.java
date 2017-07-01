package cloud.benchflow.experimentmanager.services.internal.dao;

import cloud.benchflow.experimentmanager.exceptions.TrialIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.TrialModel;
import cloud.benchflow.experimentmanager.models.TrialModel.TrialStatus;
import cloud.benchflow.faban.client.responses.RunInfo;
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

    setFabanStatus(trialID, RunStatus.StatusCode.STARTED);
  }

  public synchronized void setFabanTrialID(String trialID, String fabanRunID) {

    logger.info("setFabanTrialID: " + trialID + " with " + fabanRunID);

    TrialModel trialModel = getTrialModel(trialID);

    if (trialModel != null) {
      trialModel.setFabanRunID(fabanRunID);
      datastore.save(trialModel);
    }
  }

  public synchronized void setFabanStatus(String trialID, RunStatus.StatusCode statusCode) {

    logger.info("setFabanStatus: " + trialID + " with " + statusCode.name());

    TrialModel trialModel = getTrialModel(trialID);

    if (trialModel != null) {
      trialModel.setFabanStatus(statusCode);
      datastore.save(trialModel);
    }
  }

  public synchronized RunStatus.StatusCode getFabanStatus(String trialID) {

    logger.info("getFabanStatus: " + trialID);

    TrialModel trialModel = getTrialModel(trialID);

    return trialModel.getFabanStatus();
  }

  public synchronized void setFabanResult(String trialID, RunInfo.Result result) {

    logger.info("setFabanResult: " + trialID + " with " + result.name());

    TrialModel trialModel = getTrialModel(trialID);

    if (trialModel != null) {
      trialModel.setFabanResult(result);
      datastore.save(trialModel);
    }
  }

  public synchronized RunInfo.Result getFabanResult(String trialID) {

    logger.info("getFabanResult: " + trialID);

    TrialModel trialModel = getTrialModel(trialID);

    return trialModel.getFabanResult();
  }

  public synchronized void setTrialStatus(String trialID, TrialStatus trialStatus) {

    logger.info("setTrialStatus: " + trialID + " with " + trialStatus.name());

    TrialModel trialModel = getTrialModel(trialID);

    if (trialModel != null) {
      trialModel.setTrialStatus(trialStatus);
      datastore.save(trialModel);
    }
  }

  public synchronized TrialStatus getTrialStatus(String trialID) {

    logger.info("getTrialStatus: " + trialID);

    TrialModel trialModel = getTrialModel(trialID);

    return trialModel.getTrialStatus();
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
