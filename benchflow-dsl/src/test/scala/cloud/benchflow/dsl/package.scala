package cloud.benchflow

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 11.03.17.
 */
package object dsl {

  private val BenchFlowTestExamplesFolder = "../tests/data/dsl-examples/definition/benchflow-test/"

  val BenchFlowLoadTestExample: String = BenchFlowTestExamplesFolder + "load/benchflow-test.yml"

  val BenchFlowExhaustiveExplorationUsersExample: String = BenchFlowTestExamplesFolder + "exhaustive-exploration/one-at-a-time/users/benchflow-test.yml"
  val BenchFlowExhaustiveExplorationMultipleExample: String = BenchFlowTestExamplesFolder + "exhaustive-exploration/one-at-a-time/multiple/benchflow-test.yml"

}
