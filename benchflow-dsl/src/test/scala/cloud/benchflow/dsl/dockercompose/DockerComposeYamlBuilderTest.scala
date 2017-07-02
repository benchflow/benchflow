package cloud.benchflow.dsl.dockercompose

import cloud.benchflow.dsl.definition.types.bytes.{Bytes, BytesUnit}
import net.jcazevedo.moultingyaml._
import org.junit.{Assert, Test}
import org.scalatest.junit.JUnitSuite

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-05-25
 */
class DockerComposeYamlBuilderTest extends JUnitSuite {

  @Test
  def changeMemoryAndEnvironmentVariables(): Unit = {

    val dockerComposeYaml = DockerComposeYamlString.parseYaml

    val serviceName = "camunda"
    val underlying = 500
    val memLimit: Bytes = new Bytes(underlying = underlying, unit = BytesUnit.MEGA_BYTES)
    val environmentKey = "DB_DRIVER"
    val environmentValue = "TEST_ENVIRONMENT_VALUE"

    val newDockerComposeYamlString = new DockerComposeYamlBuilder(dockerComposeYaml)
      .memLimit(serviceName, memLimit)
      .environmentVariable(serviceName, environmentKey, environmentValue)
      .build()

    Assert.assertTrue(newDockerComposeYamlString.contains(s"mem_limit: ${memLimit.underlying}${memLimit.unit}"))
    Assert.assertTrue(newDockerComposeYamlString.contains(s"$environmentKey=$environmentValue"))
    Assert.assertFalse(newDockerComposeYamlString.contains(s"DB_DRIVER=com.mysql.jdbc.Driver"))

  }

}
