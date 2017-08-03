package cloud.benchflow.experimentmanager.tasks.running;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.FailureStatus;
import cloud.benchflow.experimentmanager.models.TrialModel.TrialStatus;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.tasks.running.CheckTrialResultTask.TrialExecutionStatus;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class CheckTrialResultTask implements Callable<TrialExecutionStatus> {

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
  public TrialExecutionStatus call() throws Exception {

    logger.info("running - " + trialID);

    try {

      TrialStatus trialStatus = trialModelDAO.getTrialStatus(trialID);

      int retries = trialModelDAO.getNumRetries(trialID);

      String experimentID = BenchFlowConstants.getExperimentIDFromTrialID(trialID);

      int maxRetries = experimentModelDAO.getNumTrialRetries(experimentID);

      FailureStatus failureStatus = null;

      switch (trialStatus) {

        // TODO - add other experiment failures

        case SUCCESS:
          // not a failure
          return TrialExecutionStatus.SUCCESS;

        case FAILED:
          failureStatus = FailureStatus.EXECUTION;
          break;

        case RANDOM_FAILURE:
          if (retries >= maxRetries) {
            failureStatus = FailureStatus.EXECUTION;
          }
          break;

        default:
          // no default
          break;

      }

      if (failureStatus == null) {
        return TrialExecutionStatus.RE_EXECUTE_TRIAL;
      } else {
        experimentModelDAO.setFailureStatus(experimentID, failureStatus);
        return TrialExecutionStatus.SUCCESS;
      }

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }

    return null;
  }

  // See for details on TrialExecutionStatus: https://github.com/benchflow/benchflow/pull/472#discussion_r131115857
  public enum TrialExecutionStatus {
    SUCCESS, RE_EXECUTE_TRIAL
  }


}
