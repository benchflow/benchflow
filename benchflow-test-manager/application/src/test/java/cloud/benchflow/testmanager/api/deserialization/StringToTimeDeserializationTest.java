package cloud.benchflow.testmanager.api.deserialization;

import cloud.benchflow.dsl.definition.types.time.Time;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.dropwizard.jackson.Jackson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-08-17
 */
public class StringToTimeDeserializationTest {

  private ObjectMapper objectMapper;

  @Before
  public void setUp() throws Exception {

    objectMapper = Jackson.newObjectMapper();
    objectMapper.registerModule(
        (new SimpleModule()).addDeserializer(Time.class, new StringToTimeDeserialization()));

  }

  @Test
  public void deserializationTest() throws Exception {

    String timeString = "1h";

    Time convertedTime = objectMapper.convertValue(timeString, Time.class);

    Assert.assertEquals(timeString, convertedTime.toString());

  }
}
