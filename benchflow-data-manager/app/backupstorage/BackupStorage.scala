package backupstorage

import java.io.InputStream
import java.io.OutputStream

import com.google.inject.ImplementedBy

case class BackupFile(id: String, name: String, fileType: String)

@ImplementedBy(classOf[GoogleDriveFromConfig])
trait BackupStorage {
  def listFiles(
    backupId: Long,
    serviceName: String,
    folderHierarchy: List[String] = Nil
  ): Option[List[BackupFile]]

  def downloadFile(
    fileId: String,
    out: OutputStream
  ): Unit

  def uploadFile(
    backupId: Long,
    serviceName: String,
    folderHierarchy: List[String],
    input: InputStream,
    length: Long,
    fileName: String,
    contentType: String
  ): Unit

  def nextId: Long
}
