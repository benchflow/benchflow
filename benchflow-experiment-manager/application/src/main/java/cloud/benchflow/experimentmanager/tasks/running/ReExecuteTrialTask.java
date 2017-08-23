package cloud.benchflow.experimentmanager.tasks.running;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.services.external.faban.FabanManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.tasks.AbortableRunnable;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class ReExecuteTrialTask extends AbortableRunnable {

  private static Logger logger = LoggerFactory.getLogger(ReExecuteTrialTask.class.getSimpleName());

  private final String trialID;
  private TrialModelDAO trialModelDAO;
  private FabanManagerService fabanManagerService;

  public ReExecuteTrialTask(String trialID) {
    this.trialID = trialID;

    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();
    this.fabanManagerService = BenchFlowExperimentManagerApplication.getFabanManagerService();
  }

  @Override
  public void run() {

    logger.info("running - " + trialID);

    // increment retries
    trialModelDAO.incrementRetries(trialID);

    ExecuteTrial.executeTrial(trialID, trialModelDAO, fabanManagerService);

  }
}
