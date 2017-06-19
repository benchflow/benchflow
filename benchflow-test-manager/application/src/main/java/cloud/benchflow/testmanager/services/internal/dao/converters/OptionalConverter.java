package cloud.benchflow.testmanager.services.internal.dao.converters;

import java.util.Optional;
import org.mongodb.morphia.converters.Converters;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;
import scala.Option;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-06-04
 */
public class OptionalConverter extends TypeConverter {

  // inspired by https://github.com/denniskuczynski/morphia_jackson_java8_optional_example/
  // blob/master/src/main/java/morphia_jackson_java8_optional/OptionalConverter.java

  private Converters converters;

  public OptionalConverter(Converters converters) {
    super(Option.class);
    this.converters = converters;
  }

  @Override
  public Object encode(Object value, MappedField optionalExtraInfo) {

    if (value == null || !(value instanceof Optional)) {
      return null;
    }

    Optional optional = (Optional) value;

    return optional.map(converters::encode).orElse(null);

  }

  @Override
  public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
    return Optional.ofNullable(fromDBObject);
  }
}
