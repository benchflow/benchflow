package cloud.benchflow.experimentmanager.tasks.running;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.FailureStatus;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class CheckTerminationCriteriaTask
    implements Callable<CheckTerminationCriteriaTask.TerminationCriteriaResult> {

  private static Logger logger =
      LoggerFactory.getLogger(CheckTerminationCriteriaTask.class.getSimpleName());

  private final String experimentID;
  private BenchFlowExperimentModelDAO experimentModelDAO;

  public CheckTerminationCriteriaTask(String experimentID) {
    this.experimentID = experimentID;
    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
  }

  @Override
  public TerminationCriteriaResult call() throws Exception {

    logger.info("running - " + experimentID);

    int numTrials = experimentModelDAO.getNumTrials(experimentID);
    long numExectuedTrials = experimentModelDAO.getNumExecutedTrials(experimentID);

    FailureStatus failureStatus = experimentModelDAO.getFailureStatus(experimentID);

    // TODO - add confidence interval (read termination criteria)

    if (failureStatus == null && numExectuedTrials < numTrials) {

      return TerminationCriteriaResult.NOT_FULFILLED;

    } else if (failureStatus != null) {

      return TerminationCriteriaResult.CANNOT_BE_FULFILLED;

    } else {

      return TerminationCriteriaResult.FULFILLED;

    }

  }

  public enum TerminationCriteriaResult {
    NOT_FULFILLED, FULFILLED, CANNOT_BE_FULFILLED
  }
}
