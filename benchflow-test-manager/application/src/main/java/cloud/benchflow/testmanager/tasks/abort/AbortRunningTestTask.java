package cloud.benchflow.testmanager.tasks.abort;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-02
 */
public class AbortRunningTestTask implements Runnable {

  private static Logger logger =
      LoggerFactory.getLogger(AbortRunningTestTask.class.getSimpleName());

  private String testID;
  private BenchFlowExperimentManagerService experimentManagerService;
  private BenchFlowTestModelDAO testModelDAO;

  public AbortRunningTestTask(String testID) {
    this.testID = testID;
    this.experimentManagerService = BenchFlowTestManagerApplication.getExperimentManagerService();
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
  }

  @Override
  public void run() {

    logger.info("running: " + testID);

    try {

      Optional<String> experimentID = testModelDAO.getRunningExperiment(testID);

      experimentID.ifPresent(id -> experimentManagerService.abortBenchFlowExperiment(id));

    } catch (BenchFlowTestIDDoesNotExistException e) {
      // nothing to do
      e.printStackTrace();
    }

  }
}
