package cloud.benchflow.faban.client.responses;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 */
@SuppressWarnings("unchecked")
public interface Response {

  default <R extends Response, T> T handle(Function<R, T> handler) {
    return handler.apply((R) this);
  }

  default <R extends Response> void handle(Consumer<R> consumer) {
    consumer.accept((R) this);
  }

}
