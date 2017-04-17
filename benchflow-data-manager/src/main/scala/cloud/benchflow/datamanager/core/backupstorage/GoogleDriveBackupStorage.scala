package cloud.benchflow.datamanager.core.backupstorage

import java.io.InputStream
import java.io.OutputStream

import scala.collection.JavaConversions._

import com.google.api.services.drive.model.File

trait GoogleDriveBackupStorage extends BackupStorage {
  val googleDrive: GoogleDrive
  val baseFolderName: String

  val baseFolder =
    googleDrive.searchFolderByName(baseFolderName)
      .headOption
      .getOrElse(googleDrive.createFolder(baseFolderName))

  val backupCount = new java.util.concurrent.atomic.AtomicLong(googleDrive.searchInFolder(baseFolder.getId).size)

  def getOrCreateFolder(name: String, baseFolder: File = this.baseFolder): File = {
    /*
     * Google Drive allows you to create 2 different folders with the same name
     * inside the same folder. So 2 different backupers will call getOrCreateFolder
     * with the same folder name and end up creating 2 different folders.
     * Synchronization prevents that.
     */
    this.synchronized {
      getFolderByNameIn(name, baseFolder.getId)
        .getOrElse(googleDrive.createFolderIn(name, baseFolder.getId))
    }
  }

  def getFolderByNameIn(name: String, baseFolderId: String): Option[File] =
    googleDrive.searchFolderByNameIn(name, baseFolderId).headOption

  def listFiles(backupId: Long, serviceName: String, folderHierarchy: List[String] = Nil): Option[List[BackupFile]] = {
    val completeHierarchy = backupId.toString :: serviceName :: folderHierarchy
    val maybeFolder =
      completeHierarchy.foldLeft(Option(baseFolder)) { (maybeFolder, folderName) =>
        maybeFolder.flatMap(x => getFolderByNameIn(folderName, x.getId))
      }
    maybeFolder.map { folder =>
      googleDrive.searchInFolder(folder.getId).map(toBackupFile(_)).toList
    }
  }

  def toBackupFile(googleFile: File): BackupFile =
    BackupFile(
      googleFile.getId,
      googleFile.getName,
      googleFile.getMimeType)

  def downloadFile(fileId: String, out: OutputStream): Unit =
    googleDrive.downloadStream(fileId, out, false)

  def uploadFile(
    backupId: Long,
    serviceName: String,
    folderHierarchy: List[String],
    input: InputStream,
    length: Long,
    fileName: String,
    contentType: String): Unit = {
    val completeHierarchy = backupId.toString :: serviceName :: folderHierarchy
    val folder =
      completeHierarchy.foldLeft(baseFolder) { (folder, folderName) =>
        getOrCreateFolder(folderName, folder)
      }
    googleDrive.uploadStream(input, length, fileName, folder.getId, contentType, false)
  }

  def nextId: Long = backupCount.getAndIncrement
}
