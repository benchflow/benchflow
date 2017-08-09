package cloud.benchflow.testmanager.services.internal.dao.converters;

import cloud.benchflow.dsl.definition.types.time.Time;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;
import scala.util.Try;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-02
 */
public class TimeConverter extends TypeConverter implements SimpleValueConverter {


  public TimeConverter() {
    super(Time.class);
  }


  @Override
  public Object encode(Object value, MappedField optionalExtraInfo) {

    if (value == null || !(value instanceof Time)) {
      return null;
    }

    Time time = (Time) value;

    return time.toString();
  }

  @Override
  public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {

    if (!(fromDBObject instanceof String)) {
      return null;
    }

    String stringValue = (String) fromDBObject;

    Try<Time> timeTry = Time.fromString(stringValue);

    if (timeTry.isFailure()) {
      return null;
    }

    return timeTry.get();
  }
}
