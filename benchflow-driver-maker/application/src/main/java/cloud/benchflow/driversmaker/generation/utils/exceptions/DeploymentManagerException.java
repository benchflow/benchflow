package cloud.benchflow.driversmaker.generation.utils.exceptions;

import com.sun.faban.driver.FatalException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 20/08/16.
 *
 * Exception for Deployment Manager interactions
 * see: http://faban.org/1.3/docs/guide/driver/errorhandling.html
 */
public class DeploymentManagerException extends FatalException {

    public DeploymentManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeploymentManagerException(String message) {
        super(message);
    }

}
