package cloud.benchflow.experimentmanager.constants;

import cloud.benchflow.experimentmanager.helpers.data.BenchFlowData;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-11
 */
public class BenchFlowConstantsTest {

  @Test
  public void getExperimentIDFromTrialID() throws Exception {

    String trialID =
        BenchFlowData.VALID_EXPERIMENT_ID_1_TRIAL + BenchFlowConstants.MODEL_ID_DELIMITER + 1;

    Assert.assertEquals(BenchFlowData.VALID_EXPERIMENT_ID_1_TRIAL,
        BenchFlowConstants.getExperimentIDFromTrialID(trialID));
  }
}
