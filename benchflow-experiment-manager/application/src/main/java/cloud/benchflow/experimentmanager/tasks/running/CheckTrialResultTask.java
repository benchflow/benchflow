package cloud.benchflow.experimentmanager.tasks.running;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.tasks.running.CheckTrialResultTask.TrialResult;
import cloud.benchflow.faban.client.responses.RunStatus;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class CheckTrialResultTask implements Callable<TrialResult> {

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
  public TrialResult call() throws Exception {

    logger.info("running - " + trialID);

    try {

      RunStatus.Code trialStatus = trialModelDAO.getTrialStatus(trialID);

      int retries = trialModelDAO.getNumRetries(trialID);

      String experimentID = BenchFlowConstants.getExperimentIDFromTrialID(trialID);

      int maxRetries = experimentModelDAO.getNumTrialRetries(experimentID);

      TrialResult trialResult = null;

      switch (trialStatus) {
        case COMPLETED:
          trialResult = TrialResult.SUCCESS;
          break;

        case FAILED:
          trialResult = TrialResult.FAILURE;
          break;

        case KILLED:
        case KILLING:
        case DENIED:
          trialResult = TrialResult.FAILURE;
          break;

        default:
          // no default
          break;

      }

      if (trialResult == TrialResult.FAILURE && retries >= maxRetries) {
        trialResult = TrialResult.EXECUTION_FAILURE;
      }

      // TODO - add other experiment failures

      return trialResult;

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }

    return null;
  }

  public enum TrialResult {
    SUCCESS, FAILURE, SUT_FAILURE, LOAD_FAILURE, EXECUTION_FAILURE, SEVERE_FAILURE
  }
}
