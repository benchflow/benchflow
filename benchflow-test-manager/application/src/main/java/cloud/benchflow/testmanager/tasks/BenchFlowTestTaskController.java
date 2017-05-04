package cloud.benchflow.testmanager.tasks;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.CompleteSelectionStrategy;
import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;
import cloud.benchflow.testmanager.tasks.running.DetermineExecuteExperimentsTask;
import cloud.benchflow.testmanager.tasks.start.StartTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.*;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20 */
public class BenchFlowTestTaskController {

  private static Logger logger =
      LoggerFactory.getLogger(BenchFlowTestTaskController.class.getSimpleName());

  private ConcurrentMap<String, Runnable> testTasks = new ConcurrentHashMap<>();

  private ExecutorService taskExecutorService;
  private BenchFlowTestModelDAO testModelDAO;
  private ExplorationModelDAO explorationModelDAO;

  public BenchFlowTestTaskController(ExecutorService taskExecutorService) {
    this.taskExecutorService = taskExecutorService;
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  // used for testing
  protected ExecutorService getTaskExecutorService() {
    return taskExecutorService;
  }

  public synchronized void startTest(
      String testID,
      String testDefinitionYamlString,
      InputStream deploymentDescriptorInputStream,
      Map<String, InputStream> bpmnModelInputStreams) {

    logger.info("startTest with testID: " + testID);

    if (testTasks.containsKey(testID)) {
      logger.info("test already started");
      return;
    }

    StartTask startTask =
        new StartTask(
            testID,
            testDefinitionYamlString,
            deploymentDescriptorInputStream,
            bpmnModelInputStreams);

    testTasks.put(testID, startTask);

    // TODO - should go into a stateless queue (so that we can recover)
    Future<?> future = taskExecutorService.submit(startTask);

    try {

      // wait for start task to complete
      future.get();

      // change state to ready
      testModelDAO.setTestState(testID, BenchFlowTestModel.BenchFlowTestState.READY);

      // move to next state
      runDetermineExecuteExperimentsTask(testID);

    } catch (InterruptedException | ExecutionException e) {
      // TODO - decide what to do in this case
      e.printStackTrace();
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since already checked before
      logger.error("test could not be found");
    }
  }

  private synchronized void runDetermineExecuteExperimentsTask(String testID) {

    logger.info("runDetermineExecuteExperimentsTask with testID: " + testID);

    if (!testTasks.containsKey(testID)) {
      logger.info("test not started");
      return;
    }

    DetermineExecuteExperimentsTask task = new DetermineExecuteExperimentsTask(testID);

    // replace with new task
    testTasks.put(testID, task);

    // set test as running
    try {
      testModelDAO.setTestState(testID, BenchFlowTestModel.BenchFlowTestState.RUNNING);
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since already checked before
      logger.error("test could not be found");
      return;
    }

    // TODO - should go into a stateless queue (so that we can recover)
    taskExecutorService.submit(task);
  }

  public synchronized void handleExperimentResult(String experimentID) {

    logger.info("handleExperimentResult for experimentID: " + experimentID);

    String testID = BenchFlowConstants.getTestIDFromExperimentID(experimentID);

    if (!testTasks.containsKey(testID)) {
      logger.info("test not started");
      return;
    }

    try {

      ExperimentSelectionStrategy selectionStrategy =
          explorationModelDAO.getExperimentSelectionStrategy(testID);

      if (selectionStrategy.getClass().equals(CompleteSelectionStrategy.class)) {

        // TODO - decide next step (run another experiment or terminate)
        boolean testComplete =
            ((CompleteSelectionStrategy) selectionStrategy).isTestComplete(testID);

        if (testComplete) {

          testModelDAO.setTestState(testID, BenchFlowTestModel.BenchFlowTestState.TERMINATED);

          testTasks.remove(testID);

        } else {
          runDetermineExecuteExperimentsTask(testID);
        }

      } else {
        // TODO
      }

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  public synchronized void testMaxTimeReached(String testID) {

    logger.info("testMaxTimeReached: " + testID);

    // TODO - implement me

  }
}
