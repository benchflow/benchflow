package cloud.benchflow.dsl.definition.errorhandling

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-03-17
 */
case class BenchFlowDeserializationException(message: String, cause: Throwable) extends Exception(message, cause)
case class BenchFlowDeserializationExceptionMessage(message: String) extends Exception(message)

// TODO - implement so that it is possible to track the traversal of the deserializations
// TODO - e.g. configuration.goal.type
