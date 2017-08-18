package cloud.benchflow.experimentmanager.tasks.running;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.services.external.faban.FabanManagerService;
import cloud.benchflow.experimentmanager.services.external.faban.FabanStatus;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.tasks.AbortableCallable;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class DetermineAndExecuteTrialsTask extends AbortableCallable<FabanStatus> {

  private static Logger logger =
      LoggerFactory.getLogger(DetermineAndExecuteTrialsTask.class.getSimpleName());
  private final String experimentID;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private TrialModelDAO trialModelDAO;
  private FabanManagerService fabanManagerService;

  public DetermineAndExecuteTrialsTask(String experimentID) {
    this.experimentID = experimentID;

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();
    this.fabanManagerService = BenchFlowExperimentManagerApplication.getFabanManagerService();
  }

  @Override
  public FabanStatus call() {

    logger.info("running - " + experimentID);

    try {
      // add trial to experiment
      String trialID = experimentModelDAO.addTrial(experimentID);

      return ExecuteTrial.executeTrial(trialID, trialModelDAO, fabanManagerService);

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }

    return null;
  }

}
