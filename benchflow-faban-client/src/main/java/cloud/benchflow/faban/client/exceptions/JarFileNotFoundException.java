package cloud.benchflow.faban.client.exceptions;


/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 28/10/15.
 */
public class JarFileNotFoundException extends Exception {

  public JarFileNotFoundException(String message) {
    super(message);
  }

  public JarFileNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
