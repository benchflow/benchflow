package cloud.benchflow.datamanager.core.backuper

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

import org.slf4j.LoggerFactory

import akka.actor._
import akka.stream.Materializer
import akka.stream.actor.{ ActorSubscriber, MaxInFlightRequestStrategy }
import akka.stream.scaladsl.{ Sink, Source }

import cloud.benchflow.datamanager.core.Backuper
import cloud.benchflow.datamanager.core.backupstorage.BackupStorage
import cloud.benchflow.datamanager.core.datarepository.cassandra.Cassandra
import play.api.libs.json.{ JsArray, JsValue, Json }

trait Data {
  def json: JsValue
}

case class JsonData(json: JsValue) extends Data {
  override def toString: String = json.toString
}

class CassandraBackuper(
    cassandra: Cassandra,
    backupStorage: BackupStorage,
    val monitor: ActorRef)(implicit system: ActorSystem, materializer: Materializer) extends Backuper {
  val serviceName = "cassandra"

  val logger = LoggerFactory.getLogger(this.getClass())

  def backup(experimentId: String, backupId: Long, jobId: Long): Unit = {
    cassandra.tables.map { table =>
      val adapter = new BackupStorageAdapter(backupStorage, serviceName, List(table))
      cassandra.getJsonStream(table, experimentId).map(stream =>
        stream
          .map(jsonString => JsonData(Json.parse(jsonString)))
          .map(DataStorageActor.Msg(_))
          .runWith(Sink.actorSubscriber(DataStorageActor.props(adapter, monitor, backupId))))
    }
    ()
  }

  def restore(backupId: Long, jobId: Long): Unit = {
    val futures = for {
      table <- cassandra.tables
      adapter = new BackupStorageAdapter(backupStorage, serviceName, List(table))
      dataItems <- adapter.read(backupId)
    } yield {
      /*
       * @TODO: Attention! This loads the entire table in one shot.
       * Use an ActorPublisher to create the stream
       */
      val stream = Source(dataItems.map(_.toString).toList)
      cassandra.putJsonStream(table, stream)
    }
    /*
     * @TODO: the completion should be reported to the monitor actor
     */
    Future.sequence(futures).onComplete({
      case Success(_) => logger.info(s"Restoring of backup $backupId completed")
      case Failure(x) => logger.error(s"Restoring of backup $backupId failed: ${x.getMessage}")
    })
    ()
  }
}

trait StorageAdapter {
  def write(id: Long, content: Seq[Data]): Unit
  def read(id: Long): Option[Seq[Data]]
}

class BackupStorageAdapter(
    backupStorage: BackupStorage,
    serviceName: String,
    folderHierarchy: List[String]) extends StorageAdapter {

  val fileCount = new java.util.concurrent.atomic.AtomicLong(0)

  def write(id: Long, content: Seq[Data]): Unit = {
    val count = fileCount.getAndIncrement

    val inputStream =
      new ByteArrayInputStream(serialize(content))
    backupStorage.uploadFile(
      id,
      serviceName,
      folderHierarchy,
      inputStream,
      content.length,
      s"file-${count}",
      "text/plain")
  }

  def read(id: Long): Option[Seq[Data]] =
    backupStorage.listFiles(id, serviceName, folderHierarchy)
      .map(_.flatMap { file =>
        val outputStream = new ByteArrayOutputStream
        backupStorage.downloadFile(file.id, outputStream)
        unserialize(outputStream.toByteArray)
      })

  def serialize(content: Seq[Data]): Array[Byte] =
    Json.prettyPrint(JsArray(content.map(_.json))).getBytes

  def unserialize(bytes: Array[Byte]): Seq[Data] =
    Json.parse(bytes).asInstanceOf[JsArray].value.map(JsonData(_))
}

object DataStorageActor {
  case class Msg(data: Data)

  def props(storage: StorageAdapter, monitor: ActorRef, id: Long): Props = Props(new DataStorageActor(storage, monitor, id))
}

class DataStorageActor(storage: StorageAdapter, monitor: ActorRef, id: Long) extends ActorSubscriber {
  import DataStorageActor._
  import akka.stream.actor.ActorSubscriberMessage._
  import cloud.benchflow.datamanager.core.BackupMonitor._

  val logger = LoggerFactory.getLogger(this.getClass())

  // TODO: make that a parameter
  val maxBufferSize = 5
  val buffer = scala.collection.mutable.ArrayBuffer.empty[Data]

  override val requestStrategy = new MaxInFlightRequestStrategy(max = maxBufferSize) {
    override def inFlightInternally: Int = buffer.size
  }

  def receive: PartialFunction[Any, Unit] = {
    case OnNext(Msg(data)) =>
      buffer += data
      assert(buffer.size <= maxBufferSize, s"bufferd too many: ${buffer.size}")
      if (buffer.size == maxBufferSize) process
    case OnComplete =>
      process
      logger.info(s"OnComplete $id")
      monitor ! Done(id)
  }

  def process: Unit = {
    storage.write(id, buffer.clone)
    buffer.clear
    monitor ! Step(id)
  }
}
