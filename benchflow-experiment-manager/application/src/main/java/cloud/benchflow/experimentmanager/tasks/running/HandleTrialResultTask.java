package cloud.benchflow.experimentmanager.tasks.running;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.faban.client.responses.RunStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public class HandleTrialResultTask implements Callable<Boolean> {

  private static Logger logger =
      LoggerFactory.getLogger(HandleTrialResultTask.class.getSimpleName());

  private final String experimentID;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private TrialModelDAO trialModelDAO;

  public HandleTrialResultTask(String experimentID) {
    this.experimentID = experimentID;
    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();
  }

  @Override
  public Boolean call() throws Exception {

    logger.info("running - " + experimentID);

    try {

      String trialID = experimentModelDAO.getLastExecutedTrialID(experimentID);

      RunStatus.Code trialStatus = trialModelDAO.getTrialStatus(trialID);

      int retries = trialModelDAO.getNumRetries(trialID);
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
