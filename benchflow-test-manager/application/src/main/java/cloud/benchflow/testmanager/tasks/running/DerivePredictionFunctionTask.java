package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.tasks.BenchFlowTestTaskController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05 */
public class DerivePredictionFunctionTask implements Runnable {

  private static Logger logger =
      LoggerFactory.getLogger(DerivePredictionFunctionTask.class.getSimpleName());

  private final String testID;

  // services
  private final BenchFlowTestTaskController testTaskController;

  public DerivePredictionFunctionTask(String testID) {
    this.testID = testID;
    this.testTaskController = BenchFlowTestManagerApplication.getTestTaskController();
  }

  @Override
  public void run() {

    logger.info("running: " + testID);

    // TODO - derive prediction function

    testTaskController.handleTestState(testID);
  }
}
