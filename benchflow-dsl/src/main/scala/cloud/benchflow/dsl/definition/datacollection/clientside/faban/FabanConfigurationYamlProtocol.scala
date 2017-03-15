package cloud.benchflow.dsl.definition.datacollection.clientside.faban

import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, YamlFormat, YamlString, YamlValue}

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 12.03.17.
  */
object FabanConfigurationYamlProtocol extends DefaultYamlProtocol {

  val MaxRunTimeKey = YamlString("max_run_time")
  val IntervalKey = YamlString("interval")
  val IntervalWorkloadKey = YamlString("interval_workload")

  implicit object ClientSideConfigurationFormat extends YamlFormat[FabanConfiguration] {

    override def read(yaml: YamlValue): FabanConfiguration = {

      val yamlObject = yaml.asYamlObject

      val maxRunTime = yamlObject.fields(MaxRunTimeKey).convertTo[String]
      val interval = yamlObject.fields(IntervalKey).convertTo[String]
      val intervalWorkload = yamlObject.fields(IntervalWorkloadKey).convertTo[Map[String, String]]

      FabanConfiguration(maxRunTime = maxRunTime, interval = interval, intervalWorkload = intervalWorkload)

    }

    override def write(obj: FabanConfiguration): YamlValue = ???
  }

}
