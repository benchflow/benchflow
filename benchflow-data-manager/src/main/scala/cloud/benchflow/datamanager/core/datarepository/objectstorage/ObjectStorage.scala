package cloud.benchflow.datamanager.core.datarepository.objectstorage

import java.io.InputStream
import java.util.Date

case class ObjectStat(bucketName: String, name: String, createdTime: Date, length: Long, contentType: String = "application/octet-stream")

trait ObjectStorage {
  def getObjectStat(bucketName: String, objectName: String): Option[ObjectStat]
  def getObject(bucketName: String, objectName: String): Option[(ObjectStat, InputStream)]
  def putObject(bucketName: String, objectName: String, stream: InputStream, size: Long, contentType: String): Option[Unit]
  def listObjects(bucketName: String, prefix: String, recursive: Boolean): List[ObjectStat]
}
