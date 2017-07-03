package cloud.benchflow.dsl.deployment.docker.service

import net.jcazevedo.moultingyaml._

import scala.collection.mutable.{ Map => MutableMap }
import scala.util.matching.Regex

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 05/07/16.
 */
object ServiceYamlProtocol extends DefaultYamlProtocol {

  implicit val imageFormat = yamlFormat1(Image)
  implicit val containerFormat = yamlFormat1(ContainerName)
  implicit val commandFormat = yamlFormat1(Command)
  implicit val volumesFormat = yamlFormat1(Volumes)
  implicit val portsFormat = yamlFormat1(Ports)
  implicit val exposeFormat = yamlFormat1(Expose)
  implicit val networkFormat = yamlFormat1(Network)
  implicit val extraHostsFormat = yamlFormat1(ExtraHosts)
  implicit val dependsOnFormat = yamlFormat1(DependsOn)
  implicit val pidFormat = yamlFormat1(Pid)

  implicit object VolumesFromYamlFormat extends YamlFormat[VolumesFrom] {

    val volumesFromRegex: Regex = "([a-zA-Z0-9_\\${}]+)(?::(ro|rw))?".r

    override def write(obj: VolumesFrom): YamlValue = {

      YamlObject(
        YamlString("volumes_from") ->
          YamlArray(
            obj.volumes.map {

              case (serviceName, None) => YamlString(serviceName)
              case (serviceName, Some(accessRights)) => YamlString(s"$serviceName:$accessRights")

            }.toVector))

    }

    override def read(yaml: YamlValue): VolumesFrom = {

      yaml match {

        case YamlArray(yamlVolumesFrom) => VolumesFrom(
          volumes = yamlVolumesFrom map { yamlVolumeFrom =>

            yamlVolumeFrom.convertTo[String] match {
              case volumesFromRegex(serviceName, null) => (serviceName, None)
              case volumesFromRegex(serviceName, accessRights) => (serviceName, Some(VolumeAccessRights(accessRights)))
            }
          })

        case _ => ???
      }

    }

  }

  implicit object EnvironmentYamlFormat extends YamlFormat[Environment] {

    override def read(yaml: YamlValue): Environment = {
      yaml.asYamlObject.fields.get(YamlString("environment")).map {
        case YamlArray(variables) =>
          new Environment(MutableMap(variables.map(_.convertTo[String]).map(EnvironmentVariable.apply).toSeq: _*))
        case _ => ???
      }
        .getOrElse(new Environment(MutableMap.empty))
    }

    override def write(env: Environment): YamlValue = {
      YamlObject(
        YamlString("environment") ->
          env.vars.toMap.map {
            case ("constraint", alias) => s"constraint:node==$alias"
            case (name, value) => s"$name=$value"
          }.toYaml)
    }
  }

  implicit object MemLimitYamlFormat extends YamlFormat[MemLimit] {
    override def read(yaml: YamlValue): MemLimit = {
      val memLimitRegex = "([1-9]+)([mg])".r
      yaml.asYamlObject.fields.get(YamlString("mem_limit")).get match {
        case YamlString(limit) =>
          limit match {
            case memLimitRegex(amount, unit) => MemLimit(amount.toInt, MemUnit(unit))
            case _ => throw new DeserializationException("Bad format for mem_limit field")
          }
        case _ =>
          throw new DeserializationException("Bad format for mem_limit field")
      }
    }

    override def write(memLimit: MemLimit): YamlValue = {
      YamlObject(YamlString("mem_limit") -> YamlString(s"${memLimit.limit}${memLimit.unit}"))
    }
  }

  implicit object CpuSetYamlFormat extends YamlFormat[CpuSet] {
    override def read(yaml: YamlValue): CpuSet = {
      val listOfCoresRegex = """^(\d+)(,\s*(\d+))*$""".r
      val rangeOfCoresRegex = """(\d+)-(\d+)""".r

      yaml.asYamlObject.fields.get(YamlString("cpuset")).get match {
        case YamlString(cpuset) =>
          cpuset match {
            case rangeOfCoresRegex(min, max) => CpuSet((max.toInt + 1) - min.toInt)
            case _ =>
              CpuSet(listOfCoresRegex.findAllIn(cpuset).toSeq.head.split(",").size)
          }
        case _ => ???
      }
    }

    override def write(cpus: CpuSet): YamlValue = {
      YamlObject(
        YamlString("cpuset") -> YamlString(s"0-${cpus.cores - 1}"))
    }
  }

  implicit object ServiceYamlFormat extends YamlFormat[Service] {

    override def write(c: Service): YamlValue = {
      val emptyMap = Map[YamlValue, YamlValue]()
      YamlObject(
        YamlString(c.name) ->
          YamlObject(

            (c.image match {
              case Some(_) => c.image.toYaml.asYamlObject.fields
              case _ => emptyMap
            })

              ++

              (c.containerName match {
                case Some(_) => c.containerName.toYaml.asYamlObject.fields
                case _ => emptyMap
              })

              ++

              (c.command match {
                case Some(_) => c.command.toYaml.asYamlObject.fields
                case _ => emptyMap
              })

              ++

              c.environment.toYaml.asYamlObject.fields

              ++

              (c.volumes match {
                case Some(_) => c.volumes.toYaml.asYamlObject.fields
                case _ => emptyMap
              })

              ++

              (c.ports match {
                case Some(_) => c.ports.toYaml.asYamlObject.fields
                case _ => emptyMap
              })

              ++

              (c.expose match {
                case Some(_) => c.expose.toYaml.asYamlObject.fields
                case _ => emptyMap
              })

              ++

              (c.net match {
                case Some(_) => c.net.toYaml.asYamlObject.fields
                case _ => emptyMap
              })

              ++

              (c.extra_hosts match {
                case Some(_) => c.extra_hosts.toYaml.asYamlObject.fields
                case _ => emptyMap
              })

              ++

              (c.cpuSet match {
                case Some(_) => c.cpuSet.toYaml.asYamlObject.fields
                case _ => emptyMap
              })

              ++

              (c.memLimit match {
                case Some(_) => c.memLimit.toYaml.asYamlObject.fields
                case _ => emptyMap
              })

              ++

              (c.volumesFrom match {
                case Some(_) => c.volumesFrom.toYaml.asYamlObject.fields
                case _ => emptyMap
              })

              ++

              (c.dependsOn match {
                case Some(_) => c.dependsOn.toYaml.asYamlObject.fields
                case _ => emptyMap
              })

              ++

              (c.pid match {
                case Some(_) => c.pid.toYaml.asYamlObject.fields
                case _ => emptyMap
              })))
    }

    override def read(value: YamlValue): Service = {
      val fields = value.asYamlObject.fields.filter(f => f._1 != YamlString("endpoints") && f._1 != YamlString("phase"))
      fields.seq.head match {
        case (YamlString(serviceName), content) =>
          val params = content.asYamlObject

          val cname = params.fields.get(YamlString("container_name")) match {
            case Some(YamlString(container_name)) => Some(ContainerName(container_name))
            case _ => None
          }

          val command = params.fields.get(YamlString("command")) match {
            case Some(YamlString(cmd)) => Some(Command(cmd))
            case _ => None
          }

          val image = params.fields.get(YamlString("image")) match {
            case Some(YamlString(img)) => Some(Image(img))
            case _ => None
          }

          val environment = params.fields.get(YamlString("environment")) match {
            case Some(YamlArray(vars)) =>
              YamlObject(
                YamlString("environment") ->
                  YamlArray(vars)).convertTo[Environment]
            case _ => Environment(MutableMap.empty[String, String])
          }

          val volumes = params.fields.get(YamlString("volumes")) match {
            case Some(YamlArray(vols)) =>
              Some(Volumes(vols.map(v => v.convertTo[String])))
            case _ => None
          }

          val ports = params.fields.get(YamlString("ports")) match {
            case Some(YamlArray(ps)) =>
              Some(Ports(ps.map(p => p.convertTo[String])))
            case _ => None
          }

          val extra_hosts = params.fields.get(YamlString("extra_hosts")) match {
            case Some(YamlArray(eh)) =>
              Some(ExtraHosts(eh.map(h => h.convertTo[String])))
            case _ => None
          }

          val dependsOn = params.fields.get(YamlString("depends_on")) match {
            case Some(YamlArray(services)) =>
              Some(DependsOn(services.map(_.convertTo[String])))
            case _ => None
          }

          val expose = params.fields.get(YamlString("expose")) match {
            case Some(YamlArray(exp)) =>
              Some(Expose(exp.map(e => e.convertTo[Int])))
            case _ => None
          }

          val network = params.fields.get(YamlString("network_mode")) match {
            case Some(YamlString(net)) => Some(Network(net))
            case _ => None
          }

          val cpuset = params.fields.get(YamlString("cpuset")) match {
            case Some(cpus) =>
              Some(YamlObject(YamlString("cpuset") -> cpus).convertTo[CpuSet])
            case _ => None
          }

          val memlimit = params.fields.get(YamlString("mem_limit")) match {
            case Some(limit) =>
              Some(YamlObject(YamlString("mem_limit") -> limit).convertTo[MemLimit])
            case _ => None
          }

          val volumesFrom = params.fields.get(YamlString("volumes_from")) match {
            case Some(vf) =>
              Some(vf.convertTo[VolumesFrom])
            case _ => None
          }

          val pid = params.fields.get(YamlString("pid")) match {
            case Some(p) =>
              Some(YamlObject(YamlString("pid") -> p).convertTo[Pid])
            case _ => None
          }

          Service(
            serviceName,
            image = image,
            containerName = cname,
            command = command,
            environment = environment,
            volumes = volumes,
            ports = ports,
            expose = expose,
            net = network,
            extra_hosts = extra_hosts,
            cpuSet = cpuset,
            memLimit = memlimit,
            volumesFrom = volumesFrom,
            dependsOn = dependsOn,
            pid = pid)
        case _ => throw DeserializationException("Invalid Docker compose file")
      }
    }

  }

}
