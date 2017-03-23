package cloud.benchflow.dsl.definition.workload.failurecriteria

import net.jcazevedo.moultingyaml.DefaultYamlProtocol

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 12.03.17.
  */
object FailureCriteriaYamlProtocol extends DefaultYamlProtocol {

  implicit val failureCriteriaFormat = yamlFormat1(FailureCriteria)

}
