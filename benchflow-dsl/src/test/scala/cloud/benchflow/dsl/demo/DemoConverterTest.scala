package cloud.benchflow.dsl.demo

import cloud.benchflow.dsl.BenchFlowDSL
import org.junit.Test
import org.scalatest.junit.JUnitSuite

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
      |        type: 'start'
      |        operations:
      |        - 11ParallelStructured.bpmn
      |
      |###############################################################################
      |# Data Collection section
      |###############################################################################
      |data_collection:
      |    server_side:
      |        activti: ['properties', 'stats']
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
      |          MYSQL_PORT: 3306
      |          COMPLETION_QUERY: SELECT+COUNT(*)+FROM+ACT_HI_PROCINST+WHERE+END_TIME_+IS+NULL
      |          COMPLETION_QUERY_VALUE: '0'
      |          COMPLETION_QUERY_METHOD: equal""".stripMargin

  @Test def convertTest(): Unit = {

    val benchFlowExperiment = BenchFlowDSL.experimentFromTestYaml(testYaml)

    val convertedExperimentYamlString: String = DemoConverter.convertExperimentToPreviousYamlString(benchFlowExperiment)

    //    val convertedYamlObject = convertedExperimentYamlString.parseYaml
    //    val expectedYamlObject = expectedExperimentYaml.parseYaml

    //    Assert.assertEquals(expectedYamlObject, convertedYamlObject)

    //    print(convertedExperimentYamlString)
    //
    //    print("============================")
    //
    //    print(convertedYamlObject.prettyPrint)
    //
    //    print("============================")
    //
    //    print(expectedYamlObject.prettyPrint)

  }

}
