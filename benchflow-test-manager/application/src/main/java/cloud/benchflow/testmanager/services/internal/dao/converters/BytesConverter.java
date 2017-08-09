package cloud.benchflow.testmanager.services.internal.dao.converters;

import cloud.benchflow.dsl.definition.types.bytes.Bytes;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;
import scala.util.Try;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-03
 */
public class BytesConverter extends TypeConverter implements SimpleValueConverter {

  public BytesConverter() {
    super(Bytes.class);
  }

  @Override
  public Object encode(Object value, MappedField optionalExtraInfo) {

    if (value == null || !(value instanceof Bytes)) {
      return null;
    }

    Bytes bytes = (Bytes) value;

    return bytes.toString();
  }

  @Override
  public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {

    if (!(fromDBObject instanceof String)) {
      return null;
    }

    String stringValue = (String) fromDBObject;

    Try<Bytes> bytesTry = Bytes.fromString(stringValue);

    if (bytesTry.isFailure()) {
      return null;
    }

    return bytesTry.get();
  }
}
