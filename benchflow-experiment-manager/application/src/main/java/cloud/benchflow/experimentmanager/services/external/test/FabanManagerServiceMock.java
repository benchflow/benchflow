package cloud.benchflow.experimentmanager.services.external.test;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.api.request.FabanStatusRequest;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants.TrialIDElements;
import cloud.benchflow.experimentmanager.resources.TrialResource;
import cloud.benchflow.experimentmanager.services.external.faban.FabanManagerService;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunInfo.Result;
import cloud.benchflow.faban.client.responses.RunStatus.StatusCode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-06-04
 */
public class FabanManagerServiceMock extends FabanManagerService {

  // the scenarios can be expressed using any case (upper,lower, camel...)
  // however in order to be used in the switch statement we can only use on case
  // by converting the received test name to lower case we can match with
  // the strings below

  // default if test name doesn't match any scenario
  private static final String SCENARIO_ALWAYS_COMPLETED = "alwayscompleted";
  private static final String SCENARIO_FAIL_FIRST_EXECUTION = "failfirstexecution";
  private static final String SCENARIO_ALWAYS_FAIL = "alwaysfail";
  private static final String SCENARIO_FAIL_EVERY_SECOND_EXPERIMENT = "faileverysecondexperiment";

  private static Logger logger =
      LoggerFactory.getLogger(FabanManagerServiceMock.class.getSimpleName());

  private Map<String, Integer> trialRunIdMap = new HashMap<>();

  private long runIdCounter = 0;

  public FabanManagerServiceMock(FabanClient fabanClient) {
    super(fabanClient, 0);
  }

  @Override
  public void deployExperimentToFaban(String experimentID, String driversMakerExperimentID,
      long experimentNumber) {
    // we do not do anything here since there is no need to deploy
    logger.info("deployExperimentToFaban: " + experimentID);
  }

  @Override
  public RunId submitTrialToFaban(String experimentID, String trialID,
      String driversMakerExperimentID, long experimentNumber) throws IOException {

    logger.info("submitTrialToFaban: " + trialID);

    // here we need to create a RunId and also store that so that we can control what to return
    String fabanID = getFabanTrialID(trialID);

    RunId runId = new RunId(fabanID, Long.toString(runIdCounter++));

    if (!trialRunIdMap.containsKey(trialID)) {
      trialRunIdMap.put(trialID, 1);
    } else {
      trialRunIdMap.put(trialID, trialRunIdMap.get(trialID) + 1);
    }

    return runId;

  }

  @Override
  public void pollForTrialStatus(String trialID, RunId runId) {

    logger.info("pollForTrialStatus: " + trialID);

    // execute in a thread to be asynchronous (similar to when faban manager is a service)
    new Thread(() -> {

      FabanStatusRequest fabanStatusRequest = null;

      // waith a little to simulate execution time
      try {
        Thread.sleep(1000);

        // here we need to return a FabanStatus depending on the scenario

        String scenario = BenchFlowConstants.getTestNameFromTrialID(trialID);

        switch (scenario.toLowerCase()) {

          case SCENARIO_ALWAYS_FAIL:

            fabanStatusRequest = new FabanStatusRequest(trialID, StatusCode.FAILED, Result.FAILED);
            break;

          case SCENARIO_FAIL_FIRST_EXECUTION:

            fabanStatusRequest = handleFailFirstExecution(trialID);
            break;

          case SCENARIO_FAIL_EVERY_SECOND_EXPERIMENT:

            fabanStatusRequest = handleFailEverySecondExperiment(trialID);
            break;

          case SCENARIO_ALWAYS_COMPLETED:
          default:

            fabanStatusRequest =
                new FabanStatusRequest(trialID, StatusCode.COMPLETED, Result.PASSED);
            break;

        }

      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {

        // in case there was some error
        if (fabanStatusRequest == null) {
          //See https://github.com/benchflow/benchflow/pull/473/files#r128371872
          fabanStatusRequest = new FabanStatusRequest(trialID, StatusCode.UNKNOWN, Result.UNKNOWN);
        }

        // send status to experiment manager
        TrialResource trialResource = BenchFlowExperimentManagerApplication.getTrialResource();

        TrialIDElements trialIDElements = new TrialIDElements(trialID);

        trialResource.setFabanResult(trialIDElements.getUsername(), trialIDElements.getTestName(),
            trialIDElements.getTestNumber(), trialIDElements.getExperimentNumber(),
            trialIDElements.getTrialNumber(), fabanStatusRequest);

      }

    }).start();

  }

  private FabanStatusRequest handleFailFirstExecution(String trialID) {

    int numExecutions = trialRunIdMap.get(trialID);

    if (numExecutions < 2) {
      return new FabanStatusRequest(trialID, StatusCode.COMPLETED, Result.UNKNOWN);
    }

    return new FabanStatusRequest(trialID, StatusCode.COMPLETED, Result.PASSED);

  }

  private FabanStatusRequest handleFailEverySecondExperiment(String trialID) {

    int experimentNumber = BenchFlowConstants.getExperimentNumberFromTrialID(trialID);

    if (experimentNumber % 2 == 0) {
      return new FabanStatusRequest(trialID, StatusCode.FAILED, Result.FAILED);
    }

    return new FabanStatusRequest(trialID, StatusCode.COMPLETED, Result.PASSED);

  }
}
