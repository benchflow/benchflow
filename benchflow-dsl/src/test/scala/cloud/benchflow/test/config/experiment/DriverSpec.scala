package cloud.benchflow.test.config.experiment

import cloud.benchflow.test.config.ConfigurationYamlProtocol
import cloud.benchflow.test.config.sut.http.{Get, HttpDriver, Put, HttpOperation}
import cloud.benchflow.test.config.sut.wfms.WfMSOperation
import org.scalatest.{Matchers, FlatSpec}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 18/07/16.
  */
class DriverSpec extends FlatSpec with Matchers with ConfigurationYamlProtocol {

  import net.jcazevedo.moultingyaml._
  import cloud.benchflow.test.config._

  "HttpOperation" should "parse correctly" in {

    val httpOperation =
      """anOperation:
        |    endpoint: http://foo.bar
        |    method: put
        |    headers:
        |        Accept: text/javascript
        |    data: payload
      """.stripMargin.parseYaml.convertTo[HttpOperation]

    val parsedHttpOperation = HttpOperation(
        name = "anOperation",
        endpoint = "http://foo.bar",
        method = Put,
        headers = Map(
          "Accept" -> "text/javascript"
        ),
        data = Some("payload")
    )

    httpOperation should be (parsedHttpOperation)

  }


  "WfMSOperation" should "parse correctly" in {

    val wfmsOperation =
      """workflow.bpmn:
        |  data: '{ "key": "value" }'
      """.stripMargin.parseYaml.convertTo[WfMSOperation]

    val parsedWfMSOperation = WfMSOperation(
      name = "workflow.bpmn",
      data = Some("""{ "key": "value" }""")
    )

    wfmsOperation should be (parsedWfMSOperation)

  }


  "HttpDriver without properties and configuration" should "parse correctly" in {

    val httpDriver =
      """http:
        |  operations:
        |  - anOperation:
        |      method: get
        |      headers:
        |        Accept: text/javascript
        |      endpoint: /operation
      """.stripMargin.parseYaml.convertTo[HttpDriver]

    val parsedHttpDriver = HttpDriver(
      operations = Vector(
        HttpOperation(
          name = "anOperation",
          method = Get,
          headers = Map("Accept" -> "text/javascript"),
          data = None,
          endpoint = "/operation"
        )
      ),
      configuration = None,
      properties = None
    )

    httpDriver should be (parsedHttpDriver)

  }


  "HttpDriver with configuration" should "parse correctly" in {

    val httpDriver =
      """
        |http:
        |  operations:
        |  - op1:
        |      method: get
        |      headers:
        |        Accept: text/javascript
        |      endpoint: /op1
        |  - op2:
        |      method: put
        |      endpoint: /op2
        |      data: payload
        |  configuration:
        |    mix:
        |      fixedSequence: [ op2, op1 ]
        |    max90th: 2
      """.stripMargin.parseYaml.convertTo[HttpDriver]

    val parsedHttpDriver = HttpDriver(
      operations = Vector(
        HttpOperation(
          name = "op1",
          method = Get,
          headers = Map("Accept" -> "text/javascript"),
          data = None,
          endpoint = "/op1"
        ),
        HttpOperation(
          name = "op2",
          method = Put,
          headers = Map.empty,
          data = Some("payload"),
          endpoint = "/op2"
        )
      ),
      configuration = Some(DriverConfiguration(
        mix = Some(FixedSequenceMix(
          sequence = Vector("op2", "op1"),
          deviation = None
        )),
        max90th = Some(2d),
        popularity = None
      )),
      properties = None
    )

    httpDriver should be (parsedHttpDriver)

  }


  "HttpDriver with properties" should "parse correctly" in {

    val httpDriver =
      """
        |http:
        |  properties:
        |    foo: bar
        |  operations:
        |  - anOperation:
        |      method: get
        |      endpoint: /operation
      """.stripMargin.parseYaml.convertTo[HttpDriver]

    val parsedHttpDriver = HttpDriver(
      properties = Some(
        Properties(properties = Map("foo" -> "bar"))
      ),
      configuration = None,
      operations = Vector(
        HttpOperation(
          name = "anOperation",
          method = Get,
          headers = Map.empty,
          data = None,
          endpoint = "/operation"
        )
      )
    )

    httpDriver should be (parsedHttpDriver)

  }

}