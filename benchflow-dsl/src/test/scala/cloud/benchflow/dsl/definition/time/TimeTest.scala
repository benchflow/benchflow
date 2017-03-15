package cloud.benchflow.dsl.definition.time

import org.junit.{Assert, Test}
import org.scalatest.junit.AssertionsForJUnit
import net.jcazevedo.moultingyaml._
import cloud.benchflow.dsl.definition.time.TimeYamlProtocol._

import scala.util.Try

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 14.03.17.
  */
class TimeTest extends AssertionsForJUnit {

  @Test def fromStringTest(): Unit = {

    Array("10h", "5m", "200s", "567ms", "90000ns").foreach(string => {

      val time = Time.fromString(string)
      Assert.assertTrue(time.isSuccess)
      Assert.assertEquals(string, time.get.toString)

    })

  }

  @Test def protocolTest(): Unit = {

    Array("10h", "5m", "200s", "567ms", "90000ns").foreach(string => {

      val time = Time.fromString(string)

      val timeYaml = time.toYaml

      Assert.assertEquals(time.get, timeYaml.convertTo[Try[Time]].get)

    })

  }

}
