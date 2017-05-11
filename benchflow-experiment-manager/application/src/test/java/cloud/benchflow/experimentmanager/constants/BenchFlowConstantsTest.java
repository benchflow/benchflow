package cloud.benchflow.experimentmanager.constants;

import cloud.benchflow.experimentmanager.helpers.TestConstants;
import org.junit.Assert;
import org.junit.Test;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-11 */
public class BenchFlowConstantsTest {
  @Test
  public void getExperimentIDFromTrialID() throws Exception {

    String trialID =
        TestConstants.BENCHFLOW_EXPERIMENT_ID + BenchFlowConstants.MODEL_ID_DELIMITER + 1;

    Assert.assertEquals(
        TestConstants.BENCHFLOW_EXPERIMENT_ID,
        BenchFlowConstants.getExperimentIDFromTrialID(trialID));
  }
}
