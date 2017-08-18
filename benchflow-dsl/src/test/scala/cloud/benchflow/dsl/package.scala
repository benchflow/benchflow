package cloud.benchflow

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch)
  *         created on 11.03.17.
  */
package object dsl {

  private val BenchFlowTestExamplesFolder = "../tests/data/dsl-examples/definition/benchflow-test/"

  val BenchFlowLoadTestExample: String = BenchFlowTestExamplesFolder + "load/benchflow-test.yml"

  private val exhaustiveFolder = "exhaustive_exploration/"
  private val oneAtATimeFolder = "one-at-a-time/"

  val BenchFlowExhaustiveExplorationUsersExample: String = BenchFlowTestExamplesFolder +
    exhaustiveFolder +
    oneAtATimeFolder +
    "users/benchflow-test.yml"

  val BenchFlowExhaustiveExplorationUsersMemoryEnvironmentExample: String = BenchFlowTestExamplesFolder +
    exhaustiveFolder +
    oneAtATimeFolder +
    "users-memory-environment/benchflow-test.yml"

  val BenchFlowExhaustiveExplorationMemoryExample: String = BenchFlowTestExamplesFolder +
    exhaustiveFolder +
    oneAtATimeFolder +
    "memory/benchflow-test.yml"

  val BenchFlowExhaustiveExplorationUsersEnvironmentExample: String = BenchFlowTestExamplesFolder +
    exhaustiveFolder +
    oneAtATimeFolder +
    "users-environment/benchflow-test.yml"

}
