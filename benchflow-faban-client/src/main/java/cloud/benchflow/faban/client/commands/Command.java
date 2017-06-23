package cloud.benchflow.faban.client.commands;

import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.responses.Response;


/**
 * Interface for a generic command.
 *
 * @author Simone D'Avico (simonedavico@gmail.com)
 */
public interface Command<T extends Response> {

  default T exec(FabanClientConfig fabanConfig) throws Throwable {
    throw new UnsupportedOperationException();
  }

}
