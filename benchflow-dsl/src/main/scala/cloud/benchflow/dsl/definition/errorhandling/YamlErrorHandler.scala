package cloud.benchflow.dsl.definition.errorhandling

import net.jcazevedo.moultingyaml.DeserializationException

import scala.util.{ Failure, Try }

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-03-16
 */
object YamlErrorHandler {

  // TODO - add comments describing this class

  private def missingKeyFailure(key: String, e: Exception) =
    Failure(BenchFlowDeserializationException("Expected key not found: " + key, e))

  private def unexpectedTypeFailure(key: String, e: Exception) =
    Failure(BenchFlowDeserializationException(
      s"Unexpected type: $key -> ${e.getMessage.take(1).toLowerCase + e.getMessage.drop(1)}",
      e))

  private def benchFlowMessageTypeFailure(key: String, e: BenchFlowDeserializationExceptionMessage) =
    Failure(
      BenchFlowDeserializationExceptionMessage(
        s"$key -> ${e.message}"))

  private def unexpectedException(e: Exception) =
    Failure(BenchFlowDeserializationException("Unexpected exception found: " + e.getMessage, e))

  private def exceptionHandler(key: String, e: Exception): Failure[Nothing] = e match {
    case e: NoSuchElementException => missingKeyFailure(key, e)
    case e: DeserializationException => unexpectedTypeFailure(key, e)
    case e: BenchFlowDeserializationExceptionMessage => benchFlowMessageTypeFailure(key, e)
    case e: BenchFlowDeserializationException => Failure(e)
    case e: Exception => unexpectedException(e)
  }

  def deserializationHandler[T](deserialization: => T, key: String): Try[T] =
    Try(deserialization) recoverWith {
      case e: Exception => exceptionHandler(key, e)
    }

  def unsupportedReadOperation: Nothing = throw new UnsupportedOperationException("To deserialize: call by wrapping in Try")

  def unsupportedWriteOperation: Nothing = throw new UnsupportedOperationException("To serialize: call without wrapping in Try")

}
