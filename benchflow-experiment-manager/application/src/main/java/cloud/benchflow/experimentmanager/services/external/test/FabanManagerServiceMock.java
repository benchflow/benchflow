package cloud.benchflow.experimentmanager.services.external.test;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.services.external.FabanManagerService;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial.TrialStatus;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.JarFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus.Code;
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
    super(fabanClient);
  }

  @Override
  public void deployExperimentToFaban(String experimentID, String driversMakerExperimentID,
      long experimentNumber) throws IOException, JarFileNotFoundException {
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
  public TrialStatus pollForTrialStatus(String trialID, RunId runId) throws RunIdNotFoundException {

    logger.info("pollForTrialStatus: " + trialID);

    // here we need to return a TrialStatus depending on the scenario

    String scenario = BenchFlowConstants.getTestNameFromTrialID(trialID);

    TrialStatus trialStatus;

    switch (scenario.toLowerCase()) {

      case SCENARIO_ALWAYS_FAIL:

        trialStatus = new TrialStatus(trialID, Code.FAILED);

        break;

      case SCENARIO_FAIL_FIRST_EXECUTION:

        trialStatus = handleFailFirstExecution(trialID);
        break;

      case SCENARIO_FAIL_EVERY_SECOND_EXPERIMENT:

        trialStatus = handleFailEverySecondExperiment(trialID);
        break;

      case SCENARIO_ALWAYS_COMPLETED:
      default:

        trialStatus = new TrialStatus(trialID, Code.COMPLETED);

        break;

    }

    return trialStatus;

  }

  private TrialStatus handleFailFirstExecution(String trialID) {

    int numExecutions = trialRunIdMap.get(trialID);

    if (numExecutions < 2) {
      return new TrialStatus(trialID, Code.FAILED);
    }

    return new TrialStatus(trialID, Code.COMPLETED);

  }

  private TrialStatus handleFailEverySecondExperiment(String trialID) {

    int experimentNumber = BenchFlowConstants.getExperimentNumberFromTrialID(trialID);

    if (experimentNumber % 2 == 0) {
      return new TrialStatus(trialID, Code.FAILED);
    }

    return new TrialStatus(trialID, Code.COMPLETED);

  }
}
