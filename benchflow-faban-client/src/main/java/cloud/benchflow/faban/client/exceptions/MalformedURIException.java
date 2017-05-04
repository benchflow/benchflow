package cloud.benchflow.faban.client.exceptions;

/**
 * Created by simonedavico on 28/10/15.
 */
public class MalformedURIException extends FabanClientException {

    public MalformedURIException(String message) {
        super(message);
    }

    public MalformedURIException(String message, Throwable cause) {
        super(message, cause);
    }
}
