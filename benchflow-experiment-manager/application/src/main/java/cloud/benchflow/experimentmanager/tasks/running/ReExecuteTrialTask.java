package cloud.benchflow.experimentmanager.tasks.running;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.responses.RunStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public class ReExecuteTrialTask implements Callable<RunStatus> {

  private static Logger logger = LoggerFactory.getLogger(ReExecuteTrialTask.class.getSimpleName());

  private final String experimentID;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private TrialModelDAO trialModelDAO;
  private MinioService minioService;
  private FabanClient fabanClient;

  public ReExecuteTrialTask(String experimentID) {
    this.experimentID = experimentID;

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();
    this.minioService = BenchFlowExperimentManagerApplication.getMinioService();
    this.fabanClient = BenchFlowExperimentManagerApplication.getFabanClient();
  }

  @Override
  public RunStatus call() throws Exception {

    logger.info("running - " + experimentID);

    try {

      // get last executed trial
      int trialNumber = experimentModelDAO.getNumExecutedTrials(experimentID);
      String trialID = BenchFlowConstants.getTrialID(experimentID, trialNumber);

      return ExecuteTrial.executeTrial(
          trialID, experimentID, trialModelDAO, minioService, fabanClient);

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }

    return null;
  }
}
