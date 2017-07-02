package cloud.benchflow.dsl.definition.types

import cloud.benchflow.dsl.definition.types.bytes.Bytes
import cloud.benchflow.dsl.definition.types.bytes.BytesYamlProtocol._
import net.jcazevedo.moultingyaml._
import org.junit.{Assert, Test}
import org.scalatest.junit.JUnitSuite

import scala.util.Try

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-23
 */
class BytesTest extends JUnitSuite {

  @Test def fromStringTest(): Unit = {

    Array("10b", "5k", "200m", "5g").foreach(string => {

      val bytes = Bytes.fromString(string)
      Assert.assertTrue(bytes.isSuccess)
      Assert.assertEquals(string, bytes.get.toString)

    })

  }

  @Test def protocolTest(): Unit = {

    Array("10b", "5k", "200m", "5g").foreach(string => {

      val time = Bytes.fromString(string).get

      val timeYaml = time.toYaml

      Assert.assertEquals(time, timeYaml.convertTo[Try[Bytes]].get)

    })

  }

}
