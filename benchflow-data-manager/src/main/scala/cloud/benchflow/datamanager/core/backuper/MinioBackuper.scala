package cloud.benchflow.datamanager.core.backuper

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }

import com.google.inject.ImplementedBy

import akka.actor.ActorRef

import cloud.benchflow.datamanager.core.Backuper
import cloud.benchflow.datamanager.core.backupstorage.{ BackupStorage, GoogleDriveFromConfig }
import cloud.benchflow.datamanager.core.datarepository.filestorage.{ ExperimentFile, ExperimentFileStorage }
import cloud.benchflow.datamanager.core.datarepository.objectstorage.MinioFromConfig

class MinioBackuper(experimentFileStorage: ExperimentFileStorage, backupStorage: BackupStorage, val monitor: ActorRef) extends Backuper {
  val serviceName = "minio"

  def backup(experimentId: String, backupId: Long): Unit = {
    experimentFileStorage.getExperimentFiles(experimentId).map {
      case ExperimentFile(name, content, length, contentType) =>
        val fileInGoogle = backupStorage.uploadFile(
          backupId,
          serviceName,
          List(),
          content,
          length,
          name,
          contentType
        )
        name
    }
    ()
  }

  def restore(backupId: Long): Unit = {
    backupStorage.listFiles(backupId, serviceName)
      .map(_.map { file =>
        val fileInGoogle = new ByteArrayOutputStream
        backupStorage.downloadFile(file.id, fileInGoogle)
        val buffer = fileInGoogle.toByteArray
        val inputStream = new ByteArrayInputStream(buffer)
        experimentFileStorage.putExperimentFile(ExperimentFile(
          file.name,
          inputStream,
          buffer.length,
          file.fileType
        ))
      })
    ()
  }

}
