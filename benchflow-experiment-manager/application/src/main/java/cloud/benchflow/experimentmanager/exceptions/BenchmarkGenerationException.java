package cloud.benchflow.experimentmanager.exceptions;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 05/03/16.
 * @author Jesper Findahl (jesper.findahl@usi.ch) - Created on 18/08/17.
 */
public class BenchmarkGenerationException extends Exception {

  private static final String MESSAGE = "Couldn't extract experiment archive for ";

  public BenchmarkGenerationException(String experimentID, int status) {
    super(MESSAGE + " for " + experimentID + " with response status " + status);
  }

}
