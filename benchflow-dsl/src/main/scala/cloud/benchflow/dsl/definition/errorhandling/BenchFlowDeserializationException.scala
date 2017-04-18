package cloud.benchflow.dsl.definition.errorhandling

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-03-17
 */
class BenchFlowDeserializationException(message: String, cause: Throwable) extends Exception

// TODO - implement so that it is possible to track the traversal of the deserializations
// TODO - e.g. configuration.goal.type
