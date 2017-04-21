import scala.collection.mutable.ArrayBuffer

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import akka.actor.{ ActorSystem, actorRef2Scala }
import akka.stream.ActorMaterializer
import akka.stream.actor.ActorSubscriberMessage.{ OnComplete, OnNext }
import akka.stream.scaladsl.{ Sink, Source }
import akka.testkit.{ ImplicitSender, TestKit }

import cloud.benchflow.datamanager.core.BackupMonitor.{ Done, Step }
import cloud.benchflow.datamanager.core.backuper.{ Data, DataStorageActor }
import cloud.benchflow.datamanager.core.backuper.DataStorageActor.Msg
import cloud.benchflow.datamanager.core.backuper.StorageAdapter
import play.api.libs.json.Json
import play.api.libs.json.JsValue

class TestStorage extends StorageAdapter {
  val store = scala.collection.mutable.Map.empty[Long, ArrayBuffer[Seq[Data]]]
  def write(id: Long, content: Seq[Data]): Unit =
    store.getOrElseUpdate(id, ArrayBuffer()) += content
  def read(id: Long): Option[Seq[Data]] = store.get(id).map(_.flatten)
  def nextId: Long = store.size
}

class DataStorageTests extends TestKit(ActorSystem("QuickStart"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  implicit val materializer = ActorMaterializer()

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  case class StringData(data: String) extends Data {
    def json: JsValue = Json.parse(data)
  }

  "data storage actor" must {
    "store data" in {
      val storage = new TestStorage

      val storageActor = system.actorOf(DataStorageActor.props(storage, testActor, 1))
      (0 to 11).map { number =>
        val data = StringData("test" + number)
        storageActor ! OnNext(Msg(data))
      }
      storageActor ! OnComplete

      expectMsg(Step(1))
      expectMsg(Step(1))
      expectMsg(Step(1))
      expectMsg(Done(1))

      storage.store should be {
        Map(
          1 -> ArrayBuffer(
            Seq(StringData("test0"), StringData("test1"), StringData("test2"), StringData("test3"), StringData("test4")),
            Seq(StringData("test5"), StringData("test6"), StringData("test7"), StringData("test8"), StringData("test9")),
            Seq(StringData("test10"), StringData("test11"))))
      }
    }

    "interact with stream" in {
      val storage = new TestStorage

      Source(1 to 3)
        .map(_.toString)
        .map(StringData)
        .map(DataStorageActor.Msg(_))
        .runWith(Sink.actorSubscriber(DataStorageActor.props(storage, testActor, 1)))

      expectMsg(Step(1))
      expectMsg(Done(1))

      storage.store should be {
        Map(1 -> ArrayBuffer(Seq(StringData("1"), StringData("2"), StringData("3"))))
      }
    }

    "deal with errors" ignore {
      /* val sinkUnderTest = Sink.head[Int] */

      /* val (probe, future) = */
      /*   TestSource.probe[Int] */
      /*     .toMat(sinkUnderTest)(Keep.both) */
      /*     .run() */
      /* probe.sendError(new Exception("boom")) */

      /* Await.ready(future, 100.millis) */
      /* val Failure(exception) = future.value.get */
      /* assert(exception.getMessage == "boom") */
    }
  }

  // "google drive storage" ignore {
  //   /* "list files" in { */
  //   /*   import scala.collection.JavaConversions._ */

  //   /*   val dataStorage = new GoogleDriveStorage("benchflow-test") */
  //   /*   dataStorage.write(0, List(StringData("test0"))) */
  //   /*   dataStorage.write(1, List(StringData("test1"))) */
  //   /*   dataStorage.listFiles.map(_.getName).toSet should be { */
  //   /*     Set("id-0-file-0", "id-1-file-0") */
  //   /*   } */
  //   /*   dataStorage.deleteBaseFolder */
  //   /* } */

  //   "store and delete data" in {
  //     val dataStorage = new BackupStorageAdapter(new GoogleDriveFromConfig)
  //     val data = List(StringData("test"))
  //     dataStorage.write(3, data)
  //     dataStorage.read(3) should be {
  //       Some(data)
  //     }
  //   }

  // }

}
