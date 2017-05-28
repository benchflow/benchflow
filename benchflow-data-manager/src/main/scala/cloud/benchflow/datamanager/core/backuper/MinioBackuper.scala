package cloud.benchflow.datamanager.core.backuper

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }

import org.slf4j.LoggerFactory

import akka.actor.ActorRef

import cloud.benchflow.datamanager.core.Backuper
import cloud.benchflow.datamanager.core.backupstorage.{ BackupStorage, GoogleDriveFromConfig }
import cloud.benchflow.datamanager.core.datarepository.filestorage.{ ExperimentFile, ExperimentFileStorage }
import cloud.benchflow.datamanager.core.datarepository.objectstorage.MinioFromConfig
import cloud.benchflow.datamanager.core.BackupMonitor._

class MinioBackuper(experimentFileStorage: ExperimentFileStorage, backupStorage: BackupStorage, val monitor: ActorRef) extends Backuper {
  val serviceName = "minio"

  val logger = LoggerFactory.getLogger(this.getClass())

  def backup(experimentId: String, backupId: Long, jobId: Long): Unit = {
    // TODO: improve that with the use of reactive streams

    logger.info(s"Backing up files of experiment [$experimentId]")
    val files = experimentFileStorage.getExperimentFiles(experimentId)
    logger.info(s"Files to backup: ${files.size}")
    files.map {
      case ExperimentFile(name, content, length, contentType) =>
        logger.debug(s"Backup of file $name started")
        backupStorage.uploadFile(
          backupId,
          serviceName,
          List(),
          content,
          length,
          name,
          contentType)
        monitor ! Step(jobId)
        logger.debug(s"Backup of file $name completed")
    }
    monitor ! Done(jobId)
    logger.info(s"Completed backup of ${files.size} files of experiment [$experimentId]")
    ()
  }

  def restore(backupId: Long, jobId: Long): Unit = {
    // TODO: improve that with the use of reactive streams

    logger.info(s"Restoring files of backup [$backupId]")
    backupStorage.listFiles(backupId, serviceName).map { files =>
      logger.info(s"Files to restore: ${files.size}")
      files.map { file =>
        logger.debug(s"Restoring file ${file.id} started")
        val fileInGoogle = new ByteArrayOutputStream
        backupStorage.downloadFile(file.id, fileInGoogle)
        val buffer = fileInGoogle.toByteArray
        val inputStream = new ByteArrayInputStream(buffer)
        experimentFileStorage.putExperimentFile(ExperimentFile(
          file.name,
          inputStream,
          buffer.length,
          file.fileType))
        monitor ! Step(jobId)
        logger.debug(s"Restoring file ${file.id} completed")
      }
      logger.info(s"Completed restoring ${files.size} files of backup [$backupId]")
    }
    monitor ! Done(jobId)
    ()
  }

}
