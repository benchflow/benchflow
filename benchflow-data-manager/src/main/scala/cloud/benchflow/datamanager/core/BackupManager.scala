package cloud.benchflow.datamanager.core

import scala.concurrent.duration.DurationInt

import com.google.inject.ImplementedBy

import akka.actor.{ Actor, ActorRef, ActorSystem, Props, actorRef2Scala }
import akka.stream.Materializer
import akka.util.Timeout
import akka.util.Timeout.durationToTimeout

import cloud.benchflow.datamanager.core.BackuperActor.{ Backup, Restore }
import cloud.benchflow.datamanager.core.backuper.{ CassandraBackuper, MinioBackuper }
import cloud.benchflow.datamanager.core.backupstorage.BackupStorage
import cloud.benchflow.datamanager.core.datarepository.cassandra.{ Cassandra, CassandraFromConfig }
import cloud.benchflow.datamanager.core.datarepository.filestorage.ExperimentFileStorage
import cloud.benchflow.datamanager.core.datarepository.objectstorage.MinioFromConfig
import javax.inject.Inject

/**
 * A Backuper knows how to backup and restore data
 * and reports progress to the monitor
 */
trait Backuper {
  val monitor: ActorRef
  def backup(experimentId: String, backupId: Long): Unit
  def restore(backupId: Long): Unit
}

class BackupManager @Inject() (
    val cassandra: Cassandra,
    googleDrive: BackupStorage,
    val minio: ExperimentFileStorage)(implicit system: ActorSystem, materializer: Materializer) {
  implicit val timeout: Timeout = 5.seconds
  val monitor = system.actorOf(Props[BackupMonitor], "monitor")

  lazy val cassandraGoogleDriveConnector = system.actorOf(
    BackuperActor.props(new CassandraBackuper(cassandra, googleDrive, monitor)),
    "cassandra-connector")
  lazy val minioGoogleDriveConnector = system.actorOf(
    BackuperActor.props(new MinioBackuper(minio, googleDrive, monitor)),
    "minio-connector")

  def backupExperiment(experimentId: String): Long = {
    val backupId = googleDrive.nextId
    cassandraGoogleDriveConnector ! Backup(experimentId, backupId)
    minioGoogleDriveConnector ! Backup(experimentId, backupId)
    backupId
  }

  def recoverBackup(backupId: Long): Long = {
    // TODO: this implementation is not correct
    minioGoogleDriveConnector ! Restore(backupId)
    cassandraGoogleDriveConnector ! Restore(backupId)
    backupId
  }
}

object BackuperActor {
  case class Backup(experimentId: String, backupId: Long)
  case class Restore(backupId: Long)

  def props(backuper: Backuper): Props = Props(new BackuperActor(backuper))
}

class BackuperActor(backuper: Backuper) extends Actor {
  import BackuperActor._

  def receive: PartialFunction[Any, Unit] = {
    case Backup(experimentId, backupId) =>
      backuper.backup(experimentId, backupId)
    case Restore(backupId) =>
      backuper.restore(backupId)
  }
}

object BackupMonitor {
  case class Step(jobId: Long)
  case class Done(jobId: Long)
  case class GetStatus(jobId: Long)

  trait Status
  case class Progress(step: Int) extends Status
  case class Finished(step: Int) extends Status

  def props: Props = Props(new BackupMonitor)
}

class BackupMonitor extends Actor {
  import BackupMonitor._
  import java.util.concurrent.ConcurrentHashMap
  import collection.JavaConverters._

  val store = (new ConcurrentHashMap[Long, Status]).asScala

  def receive: PartialFunction[Any, Unit] = {
    case Step(jobId) =>
      store.getOrElseUpdate(jobId, Progress(0)) match {
        case Progress(step) => store(jobId) = Progress(step + 1)
        // you thought it was over but others sub-backups are still happening
        case Finished(step) => store(jobId) = Progress(step + 1)
      }
    case Done(jobId) =>
      store.getOrElseUpdate(jobId, Finished(0)) match {
        case Progress(step) => store(jobId) = Finished(step)
        case _ => ()
      }
    case GetStatus(jobId) => sender ! store.get(jobId)
  }
}
