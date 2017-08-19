package cloud.benchflow.experimentmanager.exceptions;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-08-19
 */
public class BenchMarkDeploymentException extends Exception {

  public static final String MESSAGE = "Couldn't deploy experiment with id ";

  public BenchMarkDeploymentException(String experimentID, String message) {
    super(MESSAGE + experimentID + " with error message " + message);
  }

}
