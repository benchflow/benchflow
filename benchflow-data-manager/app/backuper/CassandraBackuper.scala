package backuper

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.pickling.Defaults._
import scala.pickling.binary._
import scala.pickling.binary.BinaryPickle

import akka.actor._
import akka.stream.Materializer
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.MaxInFlightRequestStrategy
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import backupstorage.BackupStorage
import datarepository.cassandra.Cassandra

trait Data

class CassandraBackuper(
    cassandra: Cassandra,
    backupStorage: BackupStorage,
    val monitor: ActorRef
)(implicit system: ActorSystem, materializer: Materializer) extends Backuper {
  val serviceName = "cassandra"

  case class JsonData(json: String) extends Data {
    // TODO: that's a hack! Fix it
    override def toString: String = json
  }

  def backup(experimentId: String, backupId: Long): Unit = {
    cassandra.tables.map { table =>
      val adapter = new BackupStorageAdapter(backupStorage, serviceName, List(table))
      cassandra.getJsonStream(table, experimentId).map(stream =>
        stream.map(JsonData(_))
          .map(DataStorageActor.Msg(_))
          .runWith(Sink.actorSubscriber(DataStorageActor.props(adapter, monitor, backupId))))
    }
    ()
  }

  def restore(backupId: Long): Unit = {
    val futures = for {
      table <- cassandra.tables
      adapter = new BackupStorageAdapter(backupStorage, serviceName, List(table))
      dataItems <- adapter.read(backupId)
    } yield {
      val stream = Source(dataItems.map(_.toString).toList)
      cassandra.putJsonStream(table, stream)
    }
    Future.sequence(futures)
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
    folderHierarchy: List[String]
) extends StorageAdapter {

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
      "text/plain"
    )
  }

  def read(id: Long): Option[Seq[Data]] =
    backupStorage.listFiles(id, serviceName, folderHierarchy)
      .map(_.flatMap { file =>
        val outputStream = new ByteArrayOutputStream
        backupStorage.downloadFile(file.id, outputStream)
        unserialize(outputStream.toByteArray)
      })

  def serialize(content: Seq[Data]): Array[Byte] =
    content.pickle.value

  def unserialize(bytes: Array[Byte]): Seq[Data] =
    BinaryPickle(bytes).unpickle[Seq[Data]]
}

object DataStorageActor {
  case class Msg(data: Data)

  def props(storage: StorageAdapter, monitor: ActorRef, id: Long): Props = Props(new DataStorageActor(storage, monitor, id))
}

class DataStorageActor(storage: StorageAdapter, monitor: ActorRef, id: Long) extends ActorSubscriber {
  import backuper.BackupMonitor._
  import DataStorageActor._
  import akka.stream.actor.ActorSubscriberMessage._

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
      monitor ! Done(id)
  }

  def process: Unit = {
    storage.write(id, buffer.clone)
    buffer.clear
    monitor ! Step(id)
  }
}
