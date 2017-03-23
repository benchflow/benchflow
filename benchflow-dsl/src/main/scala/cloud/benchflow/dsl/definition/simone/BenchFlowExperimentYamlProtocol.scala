package cloud.benchflow.dsl.definition.simone

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 23/05/16.
  */
object BenchFlowExperimentYamlProtocol extends ConfigurationYamlProtocol {

//  implicit val usersFormat = yamlFormat1(Users)
//
//  implicit object BenchFlowExperimentFormat extends YamlFormat[BenchFlowExperiment] {
//
//    override def write(bb: BenchFlowExperiment): YamlValue = {
//      YamlObject(
//        YamlString("sut") -> bb.sut.toYaml,
//        YamlString("testName") -> bb.name.toYaml,
//        YamlString("description") -> bb.description.toYaml,
//        YamlString("trials") -> bb.trials.trials.toYaml,
//        YamlString("users") -> bb.users.users.toYaml,
//        YamlString("execution") -> bb.execution.toYaml,
//        YamlString("properties") -> bb.properties.toYaml,
//        YamlString("drivers") -> {
//          bb.sut.sutType match {
//            case WfMS => bb.drivers.map(_.asInstanceOf[WfMSDriver]).toYaml
//            case Http => bb.drivers.map(_.asInstanceOf[HttpDriver]).toYaml
//          }
//        },
//        YamlString("sutConfiguration") -> bb.sutConfiguration.toYaml
//      )
//    }
//
//    override def read(yaml: YamlValue): BenchFlowExperiment = {
//
//      def getObject(key: String)(implicit obj: Map[YamlValue, YamlValue]) =
//        YamlObject(YamlString(key) -> obj.get(YamlString(key)).get)
//
//      implicit val bfBmark = yaml.asYamlObject.fields.toMap
//      val sut = yaml.asYamlObject.fields.get(YamlString("sut")).get.convertTo[Sut]
//
//      //TODO: figure out if it's possible to avoid matching again on sut type here
//      val drivers = sut.sutType match {
//        case WfMS => bfBmark.get(YamlString("drivers")).get.asInstanceOf[YamlArray].elements.map(d => d.convertTo[WfMSDriver])
//        case Http => bfBmark.get(YamlString("drivers")).get.asInstanceOf[YamlArray].elements.map(d => d.convertTo[HttpDriver])
//        case _ => throw new DeserializationException("Illegal value for type field.")
//      }
//
//      val name = bfBmark.get(YamlString("testName")).get.convertTo[String]
//      val description = bfBmark.get(YamlString("description")).get.convertTo[String]
//      val properties = getObject("properties").convertTo[Properties]
//      val sutConfig = getObject("sutConfiguration").convertTo[SutConfiguration]
//      val trials = getObject("trials").convertTo[TotalTrials]
//      val users = getObject("users").convertTo[Users]
//      val execution = bfBmark.get(YamlString("execution")).get.convertTo[LoadFunction]
//
//      BenchFlowExperiment(
//        name = name,
//        description = description,
//        sut = sut,
//        drivers = drivers,
//        properties = properties,
//        trials = trials,
//        sutConfiguration = sutConfig,
//        users = users,
//        execution = execution
//      )
//    }
//  }

}
