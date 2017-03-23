package cloud.benchflow.dsl.definition.simone

/**
  * @author Jesper Findahl (jesper.findahl@usi.ch) 
  *         created on 11.03.17.
  */
// TODO - rename to Deployment
case class Deploy(deployment: Map[String, String]) {
  def get(serviceName: String) = deployment.get(serviceName)
}
