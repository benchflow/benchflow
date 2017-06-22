package cloud.benchflow.dsl.demo

import cloud.benchflow.dsl.BenchFlowDSL
import cloud.benchflow.dsl.definition.workload.Workload
import cloud.benchflow.dsl.definition.workload.WorkloadYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{ Assert, Test }
import org.scalatest.junit.JUnitSuite

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-07
 */
class DemoConverterTest extends JUnitSuite {

  private val testYaml: String =
    """###############################################################################
      |# BenchFlow Test Definition
      |###############################################################################
      |version: '1'
      |name: ParallelMultiple11Activiti5210
      |description: ParallelMultiple11Activiti5210
      |
      |###############################################################################
      |# Test Configuration
      |###############################################################################
      |configuration:
      |    goal:
      |        type: 'load'
      |
      |    users: 50
      |
      |    workload_execution:
      |        ramp_up: 30s
      |        steady_state: 900s
      |        ramp_down: 30s
      |
      |    termination_criteria:
      |        experiment:
      |            type: 'fixed'
      |            number: 1
      |
      |###############################################################################
      |# SUT info section
      |###############################################################################
      |sut:
      |    name: activiti
      |    version: 5.21.0
      |    type: WfMS
      |
      |    configuration:
      |
      |        target_service:
      |            name: activiti
      |            endpoint: /activiti-rest
      |
      |        deployment:
      |            activiti: grid
      |            db: grid
      |
      |###############################################################################
      |# workload modeling section
      |# this is specific to the SUT type
      |###############################################################################
      |workload:
      |    my_workload:
      |        driver_type: 'start'
      |        operations:
      |        - 11ParallelStructured.bpmn
      |        - 12ParallelStructured.bpmn
      |
      |        mix:
      |            flat: [50%, 50%]
      |            max_deviation: 20%
      |
      |
      |###############################################################################
      |# Data Collection section
      |###############################################################################
      |data_collection:
      |    client_side:
      |        faban:
      |            max_run_time: 6h
      |            interval: 1s
      |    server_side:
      |        activiti: ['properties', 'stats']
      |        db:
      |            mysql:
      |                environment:
      |                    MYSQL_DB_NAME: activiti_production
      |                    MYSQL_USER: kermit
      |                    MYSQL_USER_PASSWORD: kermit
      |                    TABLE_NAMES: ACT_HI_PROCINST,ACT_HI_ACTINST
      |                    MYSQL_PORT: '3306'
      |                    COMPLETION_QUERY: SELECT+COUNT(*)+FROM+ACT_HI_PROCINST+WHERE+END_TIME_+IS+NULL
      |                    COMPLETION_QUERY_VALUE: '0'
      |                    COMPLETION_QUERY_METHOD: equal""".stripMargin

  private val expectedExperimentYaml: String =
    """sut:
      |  name: activiti
      |  type: WfMS
      |  version: 5.21.0
      |
      |testName: ParallelMultiple11Activiti5210
      |description: ParallelMultiple11Activiti5210
      |trials: 1
      |
      |users: 50
      |execution:
      |  rampUp: 30
      |  steadyState: 900
      |  rampDown: 30
      |
      |properties:
      |    stats:
      |        maxRunTime: 6
      |        interval: 1
      |
      |drivers:
      |- start:
      |    configuration:
      |      max90th: 60
      |    operations:
      |    - 11ParallelStructured.bpmn
      |    - 12ParallelStructured.bpmn
      |
      |    mix:
      |      flat: [50%, 50%]
      |      deviation: 20
      |
      |
      |sutConfiguration:
      |
      |  targetService:
      |    name: activiti
      |    endpoint: /activiti-rest
      |
      |  deploy:
      |    activiti: grid
      |    db: grid
      |
      |  benchflowConfig:
      |    activiti:
      |    - properties
      |    - stats
      |
      |    db:
      |    - mysql:
      |        config:
      |          MYSQL_DB_NAME: activiti_production
      |          MYSQL_USER: kermit
      |          MYSQL_USER_PASSWORD: kermit
      |          TABLE_NAMES: ACT_HI_PROCINST,ACT_HI_ACTINST
      |          MYSQL_PORT: '3306'
      |          COMPLETION_QUERY: SELECT+COUNT(*)+FROM+ACT_HI_PROCINST+WHERE+END_TIME_+IS+NULL
      |          COMPLETION_QUERY_VALUE: '0'
      |          COMPLETION_QUERY_METHOD: equal""".stripMargin

  @Test def convertTest(): Unit = {

    val benchFlowExperiment = BenchFlowDSL.experimentFromTestYaml(testYaml)

    val convertedExperimentYamlString: String = DemoConverter.convertExperimentToPreviousYamlString(benchFlowExperiment)

    val convertedYamlObject = convertedExperimentYamlString.parseYaml
    val expectedYamlObject = expectedExperimentYaml.parseYaml

    // TODO - it is difficult to assert that result is as expected since the order is not always the same (issue #438)
    // improve this test so it can be done automatically

    print("====== CONVERTED ========= \n")

    print(convertedYamlObject.prettyPrint)

    print("====== EXPECTED ========== \n")

    print(expectedYamlObject.prettyPrint)

    // uncomment to check manually that result is as expected
    //    Assert.assertEquals(expectedYamlObject.prettyPrint, convertedYamlObject.prettyPrint)

  }

  @Test def flatMixTest(): Unit = {

    val workloadString =
      """
        |driver_type: 'start'
        |operations:
        |- 11ParallelStructured.bpmn
        |- 12ParallelStructured.bpmn
        |
        |mix:
        |  flat: [50%, 50%]
        |  max_deviation: 20%
      """.stripMargin

    val expectedFlatMixString =
      """    mix:
        |      flat: [50%, 50%]
        |      deviation: 20
        |
        |""".stripMargin

    val stringBuilder = StringBuilder.newBuilder

    val workloadTry = workloadString.parseYaml.convertTo[Try[Workload]]

    Assert.assertTrue(workloadTry.isSuccess)

    val workload = workloadTry.get

    DemoConverter.appendDriversMix(stringBuilder, workload)

    val convertedMix = stringBuilder.toString

    Assert.assertEquals(expectedFlatMixString, convertedMix)

  }

  @Test def fixedSequenceMixTest(): Unit = {

    val workloadString =
      """
        |driver_type: 'start'
        |operations:
        |- 11ParallelStructured.bpmn
        |- 12ParallelStructured.bpmn
        |
        |mix:
        |  fixed_sequence: [11ParallelStructured.bpmn, 12ParallelStructured.bpmn]
        |  max_deviation: 20%
      """.stripMargin

    val expectedFixedSequenceMixString =
      """    mix:
        |      fixedSequence: [11ParallelStructured.bpmn, 12ParallelStructured.bpmn]
        |      deviation: 20
        |
        |""".stripMargin

    val stringBuilder = StringBuilder.newBuilder

    val workloadTry = workloadString.parseYaml.convertTo[Try[Workload]]

    Assert.assertTrue(workloadTry.isSuccess)

    val workload = workloadTry.get

    DemoConverter.appendDriversMix(stringBuilder, workload)

    val convertedMix = stringBuilder.toString

    Assert.assertEquals(expectedFixedSequenceMixString, convertedMix)

  }

  @Test def flatSequenceMixTest(): Unit = {

    val workloadString =
      """
        |driver_type: 'start'
        |operations:
        |- 11ParallelStructured.bpmn
        |- 12ParallelStructured.bpmn
        |
        |mix:
        |  flat: [40%, 60%]
        |  sequences:
        |  - [11ParallelStructured.bpmn, 12ParallelStructured.bpmn]
        |  - [12ParallelStructured.bpmn, 11ParallelStructured.bpmn]
        |  max_deviation: 20%
      """.stripMargin

    val expectedFlatSequenceMixString =
      """    mix:
        |      flat: [40%, 60%]
        |      sequences: [[11ParallelStructured.bpmn, 12ParallelStructured.bpmn], [12ParallelStructured.bpmn,
        |    11ParallelStructured.bpmn]]
        |      deviation: 20
        |
        |""".stripMargin

    val stringBuilder = StringBuilder.newBuilder

    val workloadTry = workloadString.parseYaml.convertTo[Try[Workload]]

    Assert.assertTrue(workloadTry.isSuccess)

    val workload = workloadTry.get

    DemoConverter.appendDriversMix(stringBuilder, workload)

    val convertedMix = stringBuilder.toString

    Assert.assertEquals(expectedFlatSequenceMixString, convertedMix)

  }

  @Test def matrixMixTest(): Unit = {

    val workloadString =
      """
        |driver_type: 'start'
        |operations:
        |- 11ParallelStructured.bpmn
        |- 12ParallelStructured.bpmn
        |
        |mix:
        |  matrix:
        |  - [20%,80%]
        |  - [40%,50%]
        |  max_deviation: 20%
      """.stripMargin

    val expectedMatrixMixString =
      """    mix:
        |      matrix: [[20%, 80%], [40%, 50%]]
        |      deviation: 20
        |
        |""".stripMargin

    val stringBuilder = StringBuilder.newBuilder

    val workloadTry = workloadString.parseYaml.convertTo[Try[Workload]]

    Assert.assertTrue(workloadTry.isSuccess)

    val workload = workloadTry.get

    DemoConverter.appendDriversMix(stringBuilder, workload)

    val convertedMix = stringBuilder.toString

    Assert.assertEquals(expectedMatrixMixString, convertedMix)

  }

}
