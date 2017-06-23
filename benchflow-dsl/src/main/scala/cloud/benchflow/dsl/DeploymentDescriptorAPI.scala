package cloud.benchflow.dsl

import cloud.benchflow.dsl.dockercompose.DockerComposeYamlBuilder
import net.jcazevedo.moultingyaml._

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-22
 */
object DeploymentDescriptorAPI {

  /**
   * Returns a DockerComposeYamlBuilder for creating a custom docker-compose yaml
   *
   * @param dockerComposeYamlString docker-compose.yml
   * @return
   */
  def dockerComposeYamlBuilderFromDockerComposeYaml(dockerComposeYamlString: String): DockerComposeYamlBuilder = {

    val dockerComposeYaml = dockerComposeYamlString.parseYaml

    new DockerComposeYamlBuilder(dockerComposeYaml)

  }

}
