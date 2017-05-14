package cloud.benchflow.experimentmanager.tasks.running;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial.TrialStatus;
import cloud.benchflow.faban.client.FabanClient;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public class ReExecuteTrialTask implements Callable<TrialStatus> {

  private static Logger logger = LoggerFactory.getLogger(ReExecuteTrialTask.class.getSimpleName());

  private final String trialID;
  private TrialModelDAO trialModelDAO;
  private MinioService minioService;
  private FabanClient fabanClient;

  public ReExecuteTrialTask(String trialID) {
    this.trialID = trialID;

    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();
    this.minioService = BenchFlowExperimentManagerApplication.getMinioService();
    this.fabanClient = BenchFlowExperimentManagerApplication.getFabanClient();
  }

  @Override
  public TrialStatus call() throws Exception {

    logger.info("running - " + trialID);

    // get last executed trial
    trialModelDAO.incrementRetries(trialID);

    return ExecuteTrial.executeTrial(trialID, trialModelDAO, minioService, fabanClient);
  }
}
