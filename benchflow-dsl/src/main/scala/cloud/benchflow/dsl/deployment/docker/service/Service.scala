package cloud.benchflow.dsl.deployment.docker.service

import net.jcazevedo.moultingyaml._

import scala.collection.mutable.{Map => MutableMap}

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 05/07/16.
 */
case class ContainerName(container_name: String)
case class Command(command: String)

case class Environment(vars: MutableMap[String, String])
object EnvironmentVariable {
  private val envVar = "([a-zA-Z0-9_-]+)=(.+)".r
  private val constraint = "constraint:node==([a-zA-Z]+)".r
  def apply(variable: String): (String, String) =
    variable match {
      case envVar(name, value) => (name, value)
      case constraint(alias) => ("constraint", alias)
    }
}

class EnvVar
case class Volumes(volumes: Seq[String])

sealed trait VolumeAccessRights { def toString: String }
case object ReadOnly extends VolumeAccessRights { override def toString: String = "ro" }
case object ReadWrite extends VolumeAccessRights { override def toString: String = "rw" }
object VolumeAccessRights {

  def apply(rights: String): VolumeAccessRights = rights match {
    case "ro" => ReadOnly
    case "rw" => ReadWrite
  }

}

case class VolumesFrom(volumes: Seq[(String, Option[VolumeAccessRights])])
case class DependsOn(depends_on: Seq[String])
case class Ports(ports: Seq[String])
case class Image(image: String)
case class Expose(expose: Seq[Int])
case class Network(network_mode: String)
case class ExtraHosts(extra_hosts: Seq[String])
case class CpuSet(cores: Int)
case class Pid(pid: String)

sealed trait MemUnit { def toString: String }
object MemUnit {
  def apply(unit: String): MemUnit = unit match {
    case "m" => MegaByte
    case "g" => GigaByte
  }
}
case object GigaByte extends MemUnit { override def toString: String = "g" }
case object MegaByte extends MemUnit { override def toString: String = "m" }
case class MemLimit(limit: Int, unit: MemUnit)

case class Service(
    name: String,
    image: Option[Image] = None,
    containerName: Option[ContainerName] = None,
    command: Option[Command] = None,
    environment: Environment,
    volumes: Option[Volumes] = None,
    ports: Option[Ports] = None,
    net: Option[Network] = None,
    extra_hosts: Option[ExtraHosts] = None,
    expose: Option[Expose] = None,
    cpuSet: Option[CpuSet] = None,
    memLimit: Option[MemLimit] = None,
    volumesFrom: Option[VolumesFrom] = None,
    dependsOn: Option[DependsOn] = None,
    pid: Option[Pid] = None) {

  //valid port configurations:
  //- port:port
  //- ip::port
  //- ip:port:port
  //- port

  //  private val singlePort = "([0-9]{1,5})".r
  //  private val onlyPorts = s"$singlePort:$singlePort".r
  //  private val ipPattern = s"([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})".r
  //  private val ipAndSinglePort = s"([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})::$singlePort".r
  //  private val ipAndPorts = s"$ipPattern:$singlePort:$singlePort".r

  //TODO: evaluate if these have to be changed with the ones above
  private val singlePort = "([0-9]{1,5})".r
  private val onlyPorts = s"$singlePort:$singlePort".r
  private val ipAndSinglePort = s"([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}):$singlePort".r
  private val ipAndPorts = s"$ipAndSinglePort:$singlePort".r

  def getPorts: Option[String] = {
    ports.flatMap(_.ports.head match {
      case ipAndPorts(ip, publicPort, privatePort) => Some(s"$publicPort:$privatePort")
      case onlyPorts(publicPort, privatePort) => Some(s"$publicPort:$privatePort")
      case ipAndSinglePort(ip, publicPort) => Some(publicPort)
      case singlePort(publicPort) => Some(publicPort)
    })
  }

  def getPublicPort: Option[String] = {
    ports.flatMap(_.ports.head match {
      case ipAndPorts(ip, publicPort, privatePort) => Some(s"$publicPort")
      case onlyPorts(publicPort, privatePort) => Some(s"$publicPort")
      case ipAndSinglePort(ip, publicPort) => Some(publicPort)
      case singlePort(publicPort) => Some(publicPort)
    })
  }

  def getPrivatePort: Option[String] = {
    ports.flatMap(_.ports.head match {
      case ipAndPorts(ip, publicPort, privatePort) => Some(s"$privatePort")
      case onlyPorts(publicPort, privatePort) => Some(s"$privatePort")
      case ipAndSinglePort(ip, publicPort) => None
      case singlePort(publicPort) => None
    })
  }
}
object Service {
  def fromYaml(yaml: String): Service = {
    import ServiceYamlProtocol._
    yaml.parseYaml.convertTo[Service]
  }
}
