package cloud.benchflow.experimentmanager.tasks.running;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial.TrialStatus;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.responses.RunStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public class ExecuteNewTrialTask implements Callable<TrialStatus> {

  private static Logger logger = LoggerFactory.getLogger(ExecuteNewTrialTask.class.getSimpleName());
  private final String experimentID;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private TrialModelDAO trialModelDAO;
  private MinioService minioService;
  private FabanClient fabanClient;

  public ExecuteNewTrialTask(String experimentID) {
    this.experimentID = experimentID;

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();
    this.minioService = BenchFlowExperimentManagerApplication.getMinioService();
    this.fabanClient = BenchFlowExperimentManagerApplication.getFabanClient();
  }

  @Override
  public TrialStatus call() {

    logger.info("running - " + experimentID);

    try {
      // add trial to experiment
      String trialID = experimentModelDAO.addTrial(experimentID);

      return ExecuteTrial.executeTrial(trialID, trialModelDAO, minioService, fabanClient);

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }

    return null;
  }
}
