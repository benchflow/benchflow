package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.CompleteSelectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05 */
public class ValidateTerminationCriteria implements Callable<Boolean> {

  private static Logger logger =
      LoggerFactory.getLogger(HandleExperimentResultTask.class.getSimpleName());

  private final String testID;

  private final ExplorationModelDAO explorationModelDAO;

  public ValidateTerminationCriteria(String testID) {
    this.testID = testID;
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  @Override
  public Boolean call() throws Exception {

    logger.info("running: " + testID);

    CompleteSelectionStrategy completeSelectionStrategy =
        (CompleteSelectionStrategy) explorationModelDAO.getExperimentSelectionStrategy(testID);

    return completeSelectionStrategy.isTestComplete(testID);
  }
}
