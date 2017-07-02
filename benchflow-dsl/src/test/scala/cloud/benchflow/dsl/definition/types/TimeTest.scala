package cloud.benchflow.dsl.definition.types

import cloud.benchflow.dsl.definition.types.time.Time
import cloud.benchflow.dsl.definition.types.time.TimeYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{Assert, Test}
import org.scalatest.junit.JUnitSuite

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 14.03.17.
 */
class TimeTest extends JUnitSuite {

  @Test def fromStringTest(): Unit = {

    Array("10h", "5m", "200s", "567ms", "90000ns", "4892micros").foreach(string => {

      val time = Time.fromString(string)
      Assert.assertTrue(time.isSuccess)
      Assert.assertEquals(string, time.get.toString)

    })

  }

  @Test def protocolTest(): Unit = {

    Array("10h", "5m", "200s", "567ms", "90000ns", "4892micros").foreach(string => {

      val time = Time.fromString(string).get

      val timeYaml = time.toYaml

      Assert.assertEquals(time, timeYaml.convertTo[Try[Time]].get)

    })

  }

}
