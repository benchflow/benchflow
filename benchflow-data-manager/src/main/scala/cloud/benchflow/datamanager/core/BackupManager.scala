package cloud.benchflow.datamanager.core

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import akka.actor.{ Actor, ActorRef, ActorSystem, Props, actorRef2Scala }
import akka.stream.Materializer
import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import akka.pattern.ask

import cloud.benchflow.datamanager.core.BackupMonitor._
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
  val serviceName: String
  val monitor: ActorRef
  def backup(experimentId: String, backupId: Long, jobId: Long): Unit
  def restore(backupId: Long, jobId: Long): Unit
}

class BackupManager(
    val cassandra: Cassandra,
    googleDrive: BackupStorage,
    val minio: ExperimentFileStorage)(implicit system: ActorSystem, materializer: Materializer) {

  val monitor = system.actorOf(Props[BackupMonitor], "monitor")
  lazy val backupers = List(
    new CassandraBackuper(cassandra, googleDrive, monitor),
    new MinioBackuper(minio, googleDrive, monitor))

  implicit val timeout: Timeout = 5.seconds
  val jobCount = new java.util.concurrent.atomic.AtomicLong()
  lazy val connectors = backupers.map { backuper =>
    system.actorOf(BackuperActor.props(backuper), s"${backuper.serviceName}-actor")
  }

  def backupExperiment(experimentId: String): (Long, Long) = {
    val jobId = jobCount.getAndIncrement
    val backupId = googleDrive.nextId
    connectors.map(_ ! Backup(experimentId, backupId, jobId))
    (jobId, backupId)
  }

  def recoverBackup(backupId: Long): Long = {
    val jobId = jobCount.getAndIncrement
    connectors.map(_ ! Restore(backupId, jobId))
    jobId
  }

  def getStatus(jobId: Long): Future[Option[(Int, Boolean)]] =
    (monitor ? GetStatus(jobId)).mapTo[Option[Status]].map { maybeStatus =>
      maybeStatus map {
        case Progress(step) => (step, false)
        case Finished(step) => (step, true)
      }
    }

}

object BackuperActor {
  case class Backup(experimentId: String, backupId: Long, jobId: Long)
  case class Restore(backupId: Long, jobId: Long)

  def props(backuper: Backuper): Props = Props(new BackuperActor(backuper))
}

class BackuperActor(backuper: Backuper) extends Actor {
  import BackuperActor._

  def receive: PartialFunction[Any, Unit] = {
    case Backup(experimentId, backupId, jobId) =>
      backuper.backup(experimentId, backupId, jobId)
    case Restore(backupId, jobId) =>
      backuper.restore(backupId, jobId)
  }
}

object BackupMonitor {
  case class Step(jobId: Long)
  case class Done(jobId: Long)
  case class GetStatus(jobId: Long)

  sealed trait Status
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
      store.get(jobId) match {
        case None => store(jobId) = Progress(0)
        case Some(Progress(step)) => store(jobId) = Progress(step + 1)
        // you thought it was over but others sub-backups are still happening
        case Some(Finished(step)) => store(jobId) = Progress(step + 1)
      }
    case Done(jobId) =>
      store.get(jobId) match {
        case None => store(jobId) = Finished(0)
        case Some(Progress(step)) => store(jobId) = Finished(step)
        case _ => ()
      }
    case GetStatus(jobId) => sender ! store.get(jobId)
  }
}
