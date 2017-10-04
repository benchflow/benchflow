package cloud.benchflow.testmanager.models;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 21.02.17.
 */
@Entity(noClassnameStored = true)
public class BenchFlowTestNumber {

  public static String COUNTER_FIELD_NAME = "counter";
  public static String ID_FIELD_NAME = "testIdentifier";

  @Id
  private String testIdentifier;

  private Long counter = 1L;

  BenchFlowTestNumber() {
    // Empty constructor for MongoDB + Morphia
  }

  public BenchFlowTestNumber(String userName, String benchFlowTestName) {
    this.testIdentifier = getBenchFlowTestIdentifier(userName, benchFlowTestName);
  }

  /**
   * Get the test identifier from a username and test name.
   *
   * @param userName the name of the user
   * @param benchFlowTestName the name of the test
   * @return the test identifier as a String
   */
  public static String getBenchFlowTestIdentifier(String userName, String benchFlowTestName) {

    // TODO - consider moving to BenchFlowConstants

    return userName + MODEL_ID_DELIMITER + benchFlowTestName;
  }

  public Long getCounter() {
    return counter;
  }

  public String getTestIdentifier() {
    return testIdentifier;
  }
}
