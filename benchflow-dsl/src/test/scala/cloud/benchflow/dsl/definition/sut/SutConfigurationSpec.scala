package cloud.benchflow.dsl.definition.sut

import cloud.benchflow.dsl.definition.simone.ConfigurationYamlProtocol
import org.scalatest.{FlatSpec, Matchers}

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 *         Created on 18/07/16.
 */
class SutConfigurationSpec extends FlatSpec with Matchers with ConfigurationYamlProtocol {

  //  "Deploy" should "parse correctly" in {
  //
  //    val deploy =
  //      """deploy:
  //        |    camunda: lisa1
  //        |    db: lisa2
  //      """.stripMargin.parseYaml.convertTo[Deploy]
  //
  //    val parsedDeploy = Deploy(
  //      deploy = Map(
  //        "camunda" -> "lisa1",
  //        "db" -> "lisa2"
  //      )
  //    )
  //
  //    deploy should be (parsedDeploy)
  //
  //  }

  //  "Binding" should "parse correctly" in {
  //
  //    val binding =
  //      """mysql:
  //        |  config:
  //        |    one: two
  //        |    three:
  //        |      four: five
  //        """.stripMargin.parseYaml.convertTo[Binding]
  //
  //    val parsedBinding = Binding(
  //      boundService = "mysql",
  //      config = Some(
  //        Properties(
  //          properties = Map(
  //            "one" -> "two",
  //            "three" -> Map("four" -> "five")
  //          )
  //        )
  //      )
  //    )
  //
  //    binding should be (parsedBinding)
  //
  //  }
  //
  //
  //  "BenchFlowConfig" should "parse correctly" in {
  //
  //    val benchFlowConfig =
  //      """benchflowConfig:
  //        |  camunda:
  //        |  - mysql:
  //        |      config:
  //        |        one: two
  //        |        three:
  //        |          four: five
  //        |  - stats
  //        |  db: [ stats, properties ]
  //        """.stripMargin.parseYaml.convertTo[ServerSideConfiguration]
  //
  //    val parsedBenchFlowConfig = ServerSideConfiguration(
  //      configurationMap = Map(
  //        "camunda" -> Vector(
  //          Binding(
  //            boundService = "mysql",
  //            config = Some(
  //              Properties(
  //                Map(
  //                  "one" -> "two",
  //                  "three" -> Map("four" -> "five")
  //                )
  //              )
  //            )
  //          ),
  //          Binding(
  //            boundService = "stats",
  //            config = None
  //          )
  //        ),
  //        "db" -> Vector(
  //          Binding(
  //            boundService = "stats",
  //            config = None
  //          ),
  //          Binding(
  //            boundService = "properties",
  //            config = None
  //          )
  //        )
  //      )
  //    )
  //
  //    benchFlowConfig should be (parsedBenchFlowConfig)
  //
  //  }
  //
  //
  //  "TargetService" should "parse correctly" in {
  //
  //    val targetService =
  //      """name: camunda
  //        |endpoint: /engine-rest
  //      """.stripMargin.parseYaml.convertTo[TargetService]
  //
  ////    val parsedTargetService = TargetService(
  ////      name = "camunda",
  ////      endpoint = "/engine-rest"
  ////    )
  ////
  ////    targetService should be (parsedTargetService)
  //
  //  }
  //
  //
  //  "SutConfiguration" should "parse correctly" in {
  //
  ////    val sutConfiguration =
  ////      """sutConfiguration:
  ////        |  targetService:
  ////        |    name: camunda
  ////        |    endpoint: /engine-rest
  ////        |  deploy:
  ////        |    camunda: lisa1
  ////        |  benchflowConfig:
  ////        |    camunda: [ stats, mysql ]
  ////        |    db:
  ////        |    - mysql:
  ////        |        config:
  ////        |          one: two
  ////      """.stripMargin.parseYaml.convertTo[SutConfiguration]
  //
  ////    val parsedSutConfiguration = SutConfiguration(
  ////      targetService = TargetService(
  ////        name = "camunda",
  ////        endpoint = "/engine-rest"
  ////      ),
  ////      deploy = Deploy(
  ////        deploy = Map(
  ////          "camunda" -> "lisa1"
  ////        )
  ////      ),
  ////      bfConfig = BenchFlowConfig(
  ////        benchflow_config = Map(
  ////          "camunda" -> Vector(
  ////            Binding(
  ////              boundService = "stats",
  ////              config = None
  ////            ),
  ////            Binding(
  ////              boundService = "mysql",
  ////              config = None
  ////            )
  ////          ),
  ////          "db" -> Vector(
  ////            Binding(
  ////              boundService = "mysql",
  ////              config = Some(
  ////                Properties(
  ////                  properties = Map("one" -> "two")
  ////                )
  ////              )
  ////            )
  ////          )
  ////        )
  ////      )
  ////    )
  //
  ////    sutConfiguration should be (parsedSutConfiguration)
  //
  //  }

}