package cloud.benchflow.faban.client.exceptions;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *         <p/>
 *         Created on 29/10/15.
 */
public class BenchmarkNameNotFoundRuntimeException extends FabanClientException {

  public BenchmarkNameNotFoundRuntimeException(String message) {
    super(message);
  }

  public BenchmarkNameNotFoundRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
