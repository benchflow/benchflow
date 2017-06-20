package cloud.benchflow.faban.client.exceptions;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *         <p/>
 *         Created on 29/10/15.
 */
public class BenchmarkNameNotFoundException extends FabanClientException {

  public BenchmarkNameNotFoundException(String message) {
    super(message);
  }

  public BenchmarkNameNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
