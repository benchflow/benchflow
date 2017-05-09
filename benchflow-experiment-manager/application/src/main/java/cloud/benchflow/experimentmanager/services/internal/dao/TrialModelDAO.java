package cloud.benchflow.experimentmanager.services.internal.dao;

import cloud.benchflow.experimentmanager.exceptions.TrialIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.TrialModel;
import cloud.benchflow.faban.client.responses.RunStatus;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.getTrialID;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-07 */
public class TrialModelDAO extends AbstractDAO {

  private static Logger logger = LoggerFactory.getLogger(TrialModelDAO.class.getSimpleName());

  public TrialModelDAO(MongoClient mongoClient) {
    super(mongoClient);
  }

  public synchronized void setTrialModelAsStarted(String experimentID, long trialNumber)
      throws TrialIDDoesNotExistException {

    logger.info("setTrialModelAsStarted: " + experimentID + " trial " + trialNumber);

    setTrialStatus(experimentID, trialNumber, RunStatus.Code.STARTED);
  }

  public synchronized void setFabanTrialID(
      String experimentID, long trialNumber, String fabanRunID) {

    logger.info(
        "setFabanTrialID: " + experimentID + " trial " + trialNumber + " with " + fabanRunID);

    TrialModel trialModel = getTrialModel(experimentID, trialNumber);

    if (trialModel != null) {
      trialModel.setFabanRunID(fabanRunID);
      datastore.save(trialModel);
    }
  }

  public synchronized void setTrialStatus(
      String experimentID, long trialNumber, RunStatus.Code statusCode) {

    logger.info(
        "setTrialStatus: " + experimentID + " trial " + trialNumber + " with " + statusCode.name());

    TrialModel trialModel = getTrialModel(experimentID, trialNumber);

    if (trialModel != null) {
      trialModel.setStatus(statusCode);
      datastore.save(trialModel);
    }
  }

  public synchronized RunStatus.Code getTrialStatus(String experimentID, long trialNumber) {

    logger.info("getTrialStatus: " + experimentID + " trial " + trialNumber);

    TrialModel trialModel = getTrialModel(experimentID, trialNumber);

    return trialModel.getStatus();
  }

  public synchronized void incrementRetries(String experimentID, long trialNumber) {
    logger.info("incrementRetries: " + experimentID + " trial " + trialNumber);

    TrialModel trialModel = getTrialModel(experimentID, trialNumber);

    trialModel.incrementRetries();
  }

  public synchronized int getNumRetries(String experimentID, long trialNumber) {

    logger.info("getNumRetries: " + experimentID + " trial " + trialNumber);

    TrialModel trialModel = getTrialModel(experimentID, trialNumber);

    return trialModel.getNumRetries();
  }

  private synchronized TrialModel getTrialModel(String trialID) {

    logger.info("getTrialModel: " + trialID);

    return datastore
        .createQuery(TrialModel.class)
        .field(TrialModel.ID_FIELD_NAME)
        .equal(trialID)
        .get();
  }

  private synchronized TrialModel getTrialModel(String experimentID, long trialNumber) {

    String trialID = getTrialID(experimentID, trialNumber);

    return getTrialModel(trialID);
  }
}
