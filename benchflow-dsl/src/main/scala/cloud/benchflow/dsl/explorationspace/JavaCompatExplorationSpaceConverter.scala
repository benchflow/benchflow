package cloud.benchflow.dsl.explorationspace

import java.util
import java.util.Optional

import cloud.benchflow.dsl.definition.types.bytes.Bytes
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator._

import scala.collection.JavaConverters._

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-04
 */
object JavaCompatExplorationSpaceConverter {

  // this object is here to convert to a Java compatible version
  // the collections are also converted to Java types
  // Option is replaced with Optional

  case class JavaCompatExplorationSpace(
    size: Integer,
    usersDimension: Optional[util.List[Integer]],
    memoryDimension: Optional[util.Map[String, util.List[Bytes]]],
    environment: Optional[util.Map[String, util.Map[String, util.List[String]]]])

  case class JavaCompatExplorationSpaceDimensions(
    users: Optional[util.List[Integer]],
    memory: Optional[util.Map[String, util.List[Bytes]]],
    environment: Optional[util.Map[String, util.Map[String, util.List[String]]]])

  case class JavaCompatExplorationSpacePoint(
    users: Optional[Integer],
    memory: Optional[util.Map[String, Bytes]],
    environment: Optional[util.Map[String, util.Map[String, String]]])

  def convertToJavaCompatExplorationSpace(
    explorationSpace: ExplorationSpace): JavaCompatExplorationSpace =

    JavaCompatExplorationSpace(
      explorationSpace.size,
      toOptional(explorationSpace.usersDimension.map(_.map(x => java.lang.Integer.valueOf(x)).asJava)),
      toOptional(explorationSpace.memoryDimension).map(_.mapValues(_.asJava).asJava),
      toOptional(explorationSpace.environmentDimension.map(_.mapValues(_.mapValues(_.asJava).asJava).asJava)))

  def convertFromJavaCompatExplorationSpace(
    javaCompatExplorationSpace: JavaCompatExplorationSpace): ExplorationSpace =

    ExplorationSpace(
      size = javaCompatExplorationSpace.size,
      usersDimension = toOption(javaCompatExplorationSpace.usersDimension).map(_.asScala.toList.map(_.toInt)),
      memoryDimension = toOption(javaCompatExplorationSpace.memoryDimension).map(_.asScala.toMap.mapValues(_.asScala.toList)),
      environmentDimension = toOption(javaCompatExplorationSpace.environment).map(
        _.asScala.toMap.mapValues(
          _.asScala.toMap.mapValues(
            _.asScala.toList))))

  def convertToJavaCompatExplorationSpaceDimensions(
    explorationSpaceDimensions: ExplorationSpaceDimensions): JavaCompatExplorationSpaceDimensions =

    JavaCompatExplorationSpaceDimensions(
      toOptional(explorationSpaceDimensions.users.map(_.map(x => java.lang.Integer.valueOf(x)).asJava)),
      toOptional(explorationSpaceDimensions.memory).map(_.mapValues(_.asJava).asJava),
      toOptional(explorationSpaceDimensions.environment.map(
        _.mapValues(
          _.mapValues(
            _.asJava).asJava).asJava)))

  def convertFromJavaCompatExplorationSpaceDimensions(
    javaCompatExplorationSpaceDimensions: JavaCompatExplorationSpaceDimensions): ExplorationSpaceDimensions =
    ExplorationSpaceDimensions(
      users = toOption(javaCompatExplorationSpaceDimensions.users).map(_.asScala.toList.map(_.toInt)),
      memory = toOption(javaCompatExplorationSpaceDimensions.memory).map(_.asScala.toMap.mapValues(_.asScala.toList)),
      environment = toOption(javaCompatExplorationSpaceDimensions.environment).map(
        _.asScala.toMap.mapValues(
          _.asScala.toMap.mapValues(
            _.asScala.toList))))

  def convertToJavaCompatExplorationSpacePoint(
    explorationSpacePoint: ExplorationSpacePoint): JavaCompatExplorationSpacePoint =
    JavaCompatExplorationSpacePoint(
      toOptional(explorationSpacePoint.users.map(x => java.lang.Integer.valueOf(x))),
      toOptional(explorationSpacePoint.memory.map(_.asJava)),
      toOptional(explorationSpacePoint.environment.map(_.mapValues(_.asJava).asJava)))

  def convertFromJavaCompatExplorationSpacePoint(
    javaCompatExplorationSpacePoint: JavaCompatExplorationSpacePoint): ExplorationSpacePoint =
    ExplorationSpacePoint(
      users = toOption(javaCompatExplorationSpacePoint.users).map(_.toInt),
      memory = toOption(javaCompatExplorationSpacePoint.memory).map(_.asScala.toMap),
      environment = toOption(javaCompatExplorationSpacePoint.environment).map(_.asScala.toMap.mapValues(_.asScala.toMap)))

  private def toOptional[T](opt: Option[T]): Optional[T] = opt match {
    case Some(value) => Optional.ofNullable(value)
    case None => Optional.empty()
  }

  private def toOption[T](opt: Optional[T]): Option[T] = if (opt.isPresent) Some(opt.get()) else None

}
