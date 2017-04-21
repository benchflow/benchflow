package cloud.benchflow.driversmaker.exceptions;

import javax.ws.rs.WebApplicationException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 14/03/16.
 */
public class BenchmarkGenerationException extends WebApplicationException {

    public BenchmarkGenerationException(String message, Throwable t) {
        super(message, t);
    }

    public BenchmarkGenerationException(String message) {
        super(message);
    }

}
