package cloud.benchflow.experiment.config

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 07/07/16.
  */
package object benchflowservices {

  sealed trait BenchFlowServiceType {
    def getName(id: String): String
    def getServiceName(id: String): String
  }
  case object Collector extends BenchFlowServiceType {
    override def getName(id: String): String = {
      id.split("\\.")(2)
    }

    override def getServiceName(id: String): String = {
      id.split("\\.")(3)
    }
  }
  case object Monitor extends BenchFlowServiceType {
    override def getName(id: String): String = {
      id.split("\\.")(4)
    }

    override def getServiceName(id: String): String = {
      id.split("\\.")(3)
    }
  }

  def benchFlowServiceDescriptor(serviceName: String,
                                 serviceType: BenchFlowServiceType,
                                 benchflowServicesPath: java.nio.file.Path) = {
    import scala.io.Source.fromFile

    val serviceTypeString = serviceType match {
      case Collector => "collector"
      case Monitor => "monitor"
    }

    val servicePath = benchflowServicesPath
      .resolve(serviceTypeString + "s")
      .resolve(s"$serviceName.$serviceTypeString.yml")

    fromFile(servicePath.toFile).mkString
  }

  def collectorId(serviceName: String, collectorName: String) =
    s"benchflow.collector.$collectorName.$serviceName"

  def monitorId(serviceName: String, collectorName: String, monitorName: String) =
    s"benchflow.monitor.$collectorName.$serviceName.$monitorName"

}
