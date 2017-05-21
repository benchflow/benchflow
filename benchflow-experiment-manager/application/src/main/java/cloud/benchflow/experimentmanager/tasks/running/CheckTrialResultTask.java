package cloud.benchflow.experimentmanager.tasks.running;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.faban.client.responses.RunStatus;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class CheckTrialResultTask implements Callable<Boolean> {

  private static Logger logger =
      LoggerFactory.getLogger(CheckTrialResultTask.class.getSimpleName());

  private final String trialID;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private TrialModelDAO trialModelDAO;

  public CheckTrialResultTask(String trialID) {
    this.trialID = trialID;
    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();
  }

  @Override
  public Boolean call() throws Exception {

    logger.info("running - " + trialID);

    try {

      RunStatus.Code trialStatus = trialModelDAO.getTrialStatus(trialID);

      int retries = trialModelDAO.getNumRetries(trialID);

      String experimentID = BenchFlowConstants.getExperimentIDFromTrialID(trialID);

      int maxRetries = experimentModelDAO.getNumTrialRetries(experimentID);

      // TODO - how to handle all the cases
      switch (trialStatus) {
        case COMPLETED:
          return true;

        case FAILED:
          return retries >= maxRetries;

        default:
          logger.info("case not handled: " + trialStatus);
          break;
      }

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }

    return null;
  }
}
