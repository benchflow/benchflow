package cloud.benchflow.dsl.definition.simone

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  *         Created on 19/07/16.
  */
abstract class ParameterDefinition[T](val name: String, val scope: String, val dimensionDefinition: ValueRange[T]) {

  def completeName = s"$scope.$name"

}

//parameter definition for a service
abstract class ServiceParameterDefinition[T](override val name: String, serviceName: String, override val dimensionDefinition: ValueRange[T])
  extends ParameterDefinition[T](name, serviceName, dimensionDefinition)

//an application level parameter for a service
case class ApplicationParameterDefinition[T](override val name: String, serviceName: String, override val dimensionDefinition: ValueRange[T])
  extends ServiceParameterDefinition[T](name, serviceName, dimensionDefinition)

//memory on a service
case class ServiceMemoryDefinition(serviceName: String, override val dimensionDefinition: ValueRange[Double])
  extends ServiceParameterDefinition[Double](SystemParameterDefinition.memoryDefinitionKey, serviceName, dimensionDefinition)

//cpus on a service
case class ServiceCpusDefinition(serviceName: String, override val dimensionDefinition: ValueRange[Double])
  extends ServiceParameterDefinition[Double](SystemParameterDefinition.cpusDefinitionKey, serviceName, dimensionDefinition)


//parameter definition for the whole system (do cpus and memory make sense here?)
sealed abstract class SystemParameterDefinition[T](name: String, dimensionDefinition: ValueRange[T])
  extends ParameterDefinition[T](name, "system", dimensionDefinition)
object SystemParameterDefinition {

  val cpusDefinitionKey = "cpus"
  val memoryDefinitionKey = "memory"

}


//predefined system parameter definitions
//cpus definition
case class CpusDefinition(override val dimensionDefinition: ValueRange[Double])
  extends SystemParameterDefinition[Double](SystemParameterDefinition.cpusDefinitionKey, dimensionDefinition)

//memory definition
case class MemoryDefinition(override val dimensionDefinition: ValueRange[Double])
  extends SystemParameterDefinition[Double](SystemParameterDefinition.memoryDefinitionKey, dimensionDefinition)
