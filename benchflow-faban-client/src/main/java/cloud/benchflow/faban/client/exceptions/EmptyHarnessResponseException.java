package cloud.benchflow.faban.client.exceptions;

/**
 * @author Simone D'Avico <simonedavico@gmail.com>
 *         <p/>
 *         Created on 29/10/15.
 */
public class EmptyHarnessResponseException extends FabanClientException {

    public EmptyHarnessResponseException() {
        super();
    };

    public EmptyHarnessResponseException(String message) {
        super(message);
    }

    public EmptyHarnessResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
