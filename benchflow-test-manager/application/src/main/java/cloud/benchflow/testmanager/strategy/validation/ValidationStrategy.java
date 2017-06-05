package cloud.benchflow.testmanager.strategy.validation;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-05-29
 */
public interface ValidationStrategy {

  enum Type {
    RANDOM_VALIDATION_SET
  }

  // TODO - add validation
  boolean isTestComplete(String testID);

}
