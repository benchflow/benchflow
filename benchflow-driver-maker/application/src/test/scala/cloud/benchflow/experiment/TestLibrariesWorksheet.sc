//import java.io.ByteArrayInputStream
//import java.net.{URI, URL}
//import javax.xml.parsers.DocumentBuilderFactory
//
//import cloud.benchflow.driversmaker.generation.utils.BenchmarkUtils
//import io.minio.MinioClient
//import org.apache.commons.lang3.StringEscapeUtils
//import org.w3c.dom.{NodeList, Element, Node}
//val servicesConfiguration = """<servicesConfiguration>
//                              |      <service name="camunda">
//                              |        <collectors>
//                              |          <collector name="stats">
//                              |            <id>benchflow.collector.stats.camunda</id>
//                              |            <api>
//                              |              <start>/start</start>
//                              |              <stop>/stop</stop>
//                              |            </api>
//                              |            <monitors></monitors>
//                              |          </collector>
//                              |          <collector name="logs">
//                              |            <id>benchflow.collector.logs.camunda</id>
//                              |            <api>
//                              |              <stop>/store</stop>
//                              |            </api>
//                              |            <monitors></monitors>
//                              |          </collector>
//                              |          <collector name="properties">
//                              |            <id>benchflow.collector.properties.camunda</id>
//                              |            <api>
//                              |              <stop>/store</stop>
//                              |            </api>
//                              |            <monitors></monitors>
//                              |          </collector>
//                              |          <collector name="zip">
//                              |            <id>benchflow.collector.zip.camunda</id>
//                              |            <api>
//                              |              <stop>/store</stop>
//                              |            </api>
//                              |            <monitors></monitors>
//                              |          </collector>
//                              |        </collectors>
//                              |      </service>
//                              |      <service name="db">
//                              |        <collectors>
//                              |          <collector name="mysql">
//                              |            <id>benchflow.collector.mysql.db</id>
//                              |            <api>
//                              |              <stop>/store</stop>
//                              |            </api>
//                              |            <monitors>
//                              |              <monitor name="cpu">
//                              |                <id>benchflow.monitor.mysql.db.cpu</id>
//                              |                <configuration></configuration>
//                              |                <api>
//                              |                  <monitor>/data</monitor>
//                              |                </api>
//                              |                <runPhase>all</runPhase>
//                              |              </monitor>
//                              |              <monitor name="querymysql">
//                              |                <id>benchflow.monitor.mysql.db.querymysql</id>
//                              |                <configuration>
//                              |                  <param name="QUERYMYSQL_COMPLETE_QUERY">completeQuery</param>
//                              |                </configuration>
//                              |                <api>
//                              |                  <start>/start</start>
//                              |                  <monitor>/data</monitor>
//                              |                  <stop>/stop</stop>
//                              |                </api>
//                              |                <runPhase>end</runPhase>
//                              |              </monitor>
//                              |            </monitors>
//                              |          </collector>
//                              |        </collectors>
//                              |      </service>
//                              |</servicesConfiguration>""".stripMargin
//val escapedServicesConfiguration = StringEscapeUtils.escapeXml10(servicesConfiguration)
//val unescapedServicesConfiguration = StringEscapeUtils.unescapeXml(escapedServicesConfiguration)
//val parsedServicesConfiguration = BenchmarkUtils.stringToNode(unescapedServicesConfiguration)
//val asFabanSerializedConfiguration = BenchmarkUtils.nodeToString(parsedServicesConfiguration, true, false)
//val asFabanDeserializedConfiguration = BenchmarkUtils.stringToNode(asFabanSerializedConfiguration)
//val services: NodeList = asFabanDeserializedConfiguration.getChildNodes
//for { i <- 0.until(services.getLength) } {
//  val serviceNode = services.item(i).asInstanceOf[Element]
//  val serviceName = serviceNode.getAttribute("name")
//  println(serviceName)
//  val collectorNodes = serviceNode.getElementsByTagName("collector")
//  for { j <- 0.until(collectorNodes.getLength) } {
//    val collectorNode = collectorNodes.item(j).asInstanceOf[Element]
//    val collectorName = collectorNode.getAttribute("name")
//    println(collectorName)
//    val collectorId = collectorNode.getElementsByTagName("id").item(0).getTextContent
//    println(collectorId)
//    val collectorApiNode = collectorNode.getElementsByTagName("api").item(0).asInstanceOf[Element]
//    val collectorApis = collectorApiNode.getChildNodes
//    for { apiIndex <- 0 until collectorApis.getLength } {
//      val collectorApi = collectorApis.item(apiIndex).asInstanceOf[Element]
//      collectorApi.getTagName match {
//        case "start" => println(collectorApi.getTextContent)
//        case "stop" =>  println(collectorApi.getTextContent)
//      }
//    }
//    val monitorNodes = collectorNode.getElementsByTagName("monitors").item(0).asInstanceOf[Element].getChildNodes
//    for { k <- 0 until monitorNodes.getLength } {
//      val monitorNode = monitorNodes.item(k).asInstanceOf[Element]
//      val monitorName = monitorNode.getAttribute("name")
//      val monitorId = monitorNode.getElementsByTagName("id").item(0).asInstanceOf[Element].getTextContent
//      println(monitorName)
//      println(monitorId)
//      val monitorConfig = monitorNode.getElementsByTagName("configuration").item(0).asInstanceOf[Element]
//      val params = monitorConfig.getChildNodes
//      for { monitorConfigIndex <- 0 until params.getLength } {
//        val param = params.item(monitorConfigIndex).asInstanceOf[Element]
//        val paramName = param.getAttribute("name")
//        val paramValue = param.getTextContent
//        println(paramName)
//        println(paramValue)
//      }
//    }
//  }
//}
//val fromFaban = """lt;servicesConfiguration&amp;gt;&amp;lt;service name=&amp;quot;outyet&amp;quot;&amp;gt;&amp;lt;collectors&amp;gt;&amp;lt;collector name=&amp;quot;stats&amp;quot;&amp;gt;&amp;lt;id&amp;gt;benchflow.collector.stats.outyet&amp;lt;/id&amp;gt;&amp;lt;api&amp;gt;&amp;lt;start&amp;gt;/start&amp;lt;/start&amp;gt;&amp;lt;stop&amp;gt;/stop&amp;lt;/stop&amp;gt;&amp;lt;/api&amp;gt;&amp;lt;monitors/&amp;gt;&amp;lt;/collector&amp;gt;&amp;lt;/collectors&amp;gt;&amp;lt;/service&amp;gt;&amp;lt;/servicesConfiguration&amp;gt;&amp;lt;/benchFlowServices&amp;gt;""".stripMargin
//BenchmarkUtils.stringToNode(StringEscapeUtils.unescapeXml(fromFaban))
//"ciaociao\n/stop".trim.replaceAll("\\s+", "")

