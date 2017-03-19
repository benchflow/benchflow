package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Merge
import akka.util.Timeout
import akka.pattern.ask

import javax.inject.Inject
import javax.inject.Singleton

import play.api.libs.json.Json
import play.api.mvc._

import backuper.BackupMonitor
import backuper.BackupMonitor.GetStatus
import backuper.DataStorageActor
import backuper.CassandraBackuper
import backuper.MinioBackuper

import datarepository.cassandra.Cassandra
import datarepository.filestorage.ExperimentFileStorage
import backupstorage.BackupStorage
import backuper.BackupManager

@Singleton
class Application @Inject() (backupManager: BackupManager)(implicit system: ActorSystem, materializer: Materializer) extends Controller {
  implicit val timeout: Timeout = 5.seconds

  def index: Action[AnyContent] = Action {
    Ok("It works!")
  }

  // for demonstration purposes only
  def getExperimentData(experimentId: String): Action[AnyContent] = Action {
    val minio = backupManager.minio
    val cassandra = backupManager.cassandra
    val fromMinio =
      Source(minio.experimentFileNames(experimentId))
        .map("minio: " + _.toString)
    val fromCassandra =
      cassandra.tables.flatMap { table =>
        cassandra.getJsonStream(table, experimentId).map(stream =>
          stream.map(json => s"cassandra: ${table}: ${json.toString}"))
      }
    val stream = fromCassandra match {
      case Nil => fromMinio
      case one :: Nil => Source.combine(fromMinio, one)(Merge(_))
      case one :: more => Source.combine(fromMinio, one, more: _*)(Merge(_))
    }
    Ok.chunked(stream.map(_ + "\n"))
  }

  def backupExperiment(experimentId: String): Action[AnyContent] = Action {
    val jobId = backupManager.backupExperiment(experimentId)
    Ok(Json.obj("job" -> jobId))
  }

  def recoverBackup(backupId: Long): Action[AnyContent] = Action {
    val jobId = backupManager.recoverBackup(backupId)
    Ok(Json.obj("job" -> jobId))
  }

  def backupStatus(jobId: Long): Action[AnyContent] = Action.async {
    (backupManager.monitor ? GetStatus(jobId)).mapTo[Option[BackupMonitor.Status]].map {
      case Some(status) => Ok(status.toString)
      case None => NotFound
    }
  }
}
