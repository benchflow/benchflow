package cloud.benchflow.datamanager.core.datarepository.objectstorage

import java.io.InputStream

import scala.collection.JavaConversions._
import scala.util.Try

import cloud.benchflow.datamanager.core.datarepository.filestorage.{ ExperimentFile, ExperimentFileStorage }
import io.minio.MinioClient

trait Minio extends ObjectStorage {
  val minioClient: MinioClient

  def getObjectStat(bucketName: String, objectName: String): Option[ObjectStat] =
    Try(convertObjectStat(minioClient.statObject(bucketName, objectName))).toOption

  def getObject(bucketName: String, objectName: String): Option[(ObjectStat, InputStream)] =
    for {
      fileStat <- getObjectStat(bucketName, objectName)
      fileStream <- Try(minioClient.getObject(bucketName, objectName)).toOption
    } yield (fileStat, fileStream)

  def putObject(bucketName: String, objectName: String, stream: InputStream, size: Long, contentType: String): Option[Unit] =
    Try(minioClient.putObject(bucketName, objectName, stream, size, contentType)).toOption

  def listObjects(bucketName: String, prefix: String, recursive: Boolean): List[ObjectStat] =
    for {
      result <- minioClient.listObjects(bucketName, prefix, recursive).toList
      item <- Try(result.get).toOption
    } yield {
      ObjectStat(bucketName, item.objectName, item.lastModified, item.size)
    }

  def convertObjectStat(minioObject: io.minio.ObjectStat): ObjectStat =
    ObjectStat(minioObject.bucketName, minioObject.name, minioObject.createdTime, minioObject.length, minioObject.contentType)
}

trait ExperimentObjectStorage extends ExperimentFileStorage with ObjectStorage {
  val defaultBucket: String

  def experimentFileNames(experimentId: String): List[String] =
    listObjects(defaultBucket, experimentId, false).map(_.name)

  def getExperimentFiles(experimentId: String): List[ExperimentFile] =
    for {
      fileName <- experimentFileNames(experimentId)
      (fileStat, fileStream) <- getObject(defaultBucket, fileName)
    } yield fileStat match {
      case ObjectStat(_, name, _, length, contentType) =>
        ExperimentFile(name, fileStream, length, contentType)
    }

  def putExperimentFile(file: ExperimentFile): Unit = file match {
    case ExperimentFile(name, content, length, contentType) =>
      putObject(defaultBucket, name, content, length, contentType)
      Unit
  }
}
