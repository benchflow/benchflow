package cloud.benchflow.dsl.explorationspace

import java.util.Optional

import cloud.benchflow.dsl.definition.types.bytes.Bytes
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator._
import cloud.benchflow.dsl.explorationspace.javatypes.{JavaCompatExplorationSpace, JavaCompatExplorationSpaceDimensions, JavaCompatExplorationSpacePoint}

import scala.collection.JavaConverters._
import scala.util.Success

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-04
 */
object JavaCompatExplorationSpaceConverter {

  type ByteValueAsString = String

  // this object is here to convert to a Java compatible version to be able to store in the DB
  // the collections are also converted to Java types
  // Option is replaced with Optional

  def convertToJavaCompatExplorationSpace(
    explorationSpace: ExplorationSpace): JavaCompatExplorationSpace =

    new JavaCompatExplorationSpace(
      explorationSpace.size,
      toOptional(explorationSpace.usersDimension.map(_.map(x => java.lang.Integer.valueOf(x)).asJava)),
      toOptional(explorationSpace.memoryDimension).map(_.mapValues(_.asJava).asJava),
      toOptional(explorationSpace.environmentDimension.map(_.mapValues(_.mapValues(_.asJava).asJava).asJava)))

  def convertFromJavaCompatExplorationSpace(
    javaCompatExplorationSpace: JavaCompatExplorationSpace): ExplorationSpace =

    new ExplorationSpace(
      size = javaCompatExplorationSpace.getSize,
      usersDimension = toOption(javaCompatExplorationSpace.getUsersDimension).map(_.asScala.toList.map(_.toInt)),
      memoryDimension = toOption(javaCompatExplorationSpace.getMemoryDimension).map(_.asScala.toMap.mapValues(_.asScala.toList)),
      environmentDimension = toOption(javaCompatExplorationSpace.getEnvironmentDimension).map(
        _.asScala.toMap.mapValues(
          _.asScala.toMap.mapValues(
            _.asScala.toList))))

  def convertToJavaCompatExplorationSpaceDimensions(
    explorationSpaceDimensions: ExplorationSpaceDimensions): JavaCompatExplorationSpaceDimensions =

    new JavaCompatExplorationSpaceDimensions(
      toOptional(explorationSpaceDimensions.users.map(_.map(x => java.lang.Integer.valueOf(x)).asJava)),
      toOptional(explorationSpaceDimensions.memory).map(_.mapValues(_.asJava).asJava),
      toOptional(explorationSpaceDimensions.environment.map(
        _.mapValues(
          _.mapValues(
            _.asJava).asJava).asJava)))

  def convertFromJavaCompatExplorationSpaceDimensions(
    javaCompatExplorationSpaceDimensions: JavaCompatExplorationSpaceDimensions): ExplorationSpaceDimensions =
    ExplorationSpaceDimensions(
      users = toOption(javaCompatExplorationSpaceDimensions.getUsers).map(_.asScala.toList.map(_.toInt)),
      memory = toOption(javaCompatExplorationSpaceDimensions.getMemory).map(_.asScala.toMap.mapValues(_.asScala.toList)),
      environment = toOption(javaCompatExplorationSpaceDimensions.getEnvironment).map(
        _.asScala.toMap.mapValues(
          _.asScala.toMap.mapValues(
            _.asScala.toList))))

  def convertToJavaCompatExplorationSpacePoint(
    explorationSpacePoint: ExplorationSpacePoint): JavaCompatExplorationSpacePoint =
    new JavaCompatExplorationSpacePoint(
      toOptional(explorationSpacePoint.users.map(x => java.lang.Integer.valueOf(x))),
      toOptional(explorationSpacePoint.memory.map(_.asJava)),
      toOptional(explorationSpacePoint.environment.map(_.mapValues(_.asJava).asJava)))

  def convertFromJavaCompatExplorationSpacePoint(
    javaCompatExplorationSpacePoint: JavaCompatExplorationSpacePoint): ExplorationSpacePoint =
    ExplorationSpacePoint(
      users = toOption(javaCompatExplorationSpacePoint.getUsers).map(_.toInt),
      memory = toOption(javaCompatExplorationSpacePoint.getMemory).map(_.asScala.toMap),
      environment = toOption(javaCompatExplorationSpacePoint.getEnvironment).map(_.asScala.toMap.mapValues(_.asScala.toMap)))

  private def toOptional[T](opt: Option[T]): Optional[T] = opt match {
    case Some(value) => Optional.ofNullable(value)
    case None => Optional.empty()
  }

  private def toOption[T](opt: Optional[T]): Option[T] = if (opt.isPresent) Some(opt.get()) else None

  private def mapMemoryListToJavaCompat(memory: Option[Map[ServiceName, List[Bytes]]]) =
    memory.map(_.mapValues(list => list.map(_.toString)))

  private def mapMemoryListFromJavaCompat(memory: Option[Map[ServiceName, List[ByteValueAsString]]]) =
    memory.map(
      _.mapValues(
        list => list.map(
          value => Bytes.fromString(value) match {
            case Success(byte: Bytes) => byte
            //              case Failure => // TODO - issue #389
          })))

}
