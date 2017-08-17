package cloud.benchflow.testmanager.api.deserialization;

import cloud.benchflow.dsl.definition.types.time.Time;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import scala.util.Try;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-08-17
 */
public class StringToTimeDeserialization extends JsonDeserializer<Time> {

  @Override
  public Time deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {

    String timeString = p.getValueAsString();

    if (timeString == null) {
      return null;
    }

    Try<Time> timeTry = Time.fromString(timeString);

    if (timeTry.isFailure()) {
      return null;
    }

    return timeTry.get();

  }
}
