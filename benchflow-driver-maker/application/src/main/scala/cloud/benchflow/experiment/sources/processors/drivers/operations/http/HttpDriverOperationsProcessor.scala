package cloud.benchflow.experiment.sources.processors.drivers.operations.http

import cloud.benchflow.experiment.GenerationDefaults
import cloud.benchflow.experiment.sources.processors._
import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import cloud.benchflow.test.config.sut.http._
import com.sun.faban.driver.{Timing, BenchmarkOperation}
import spoon.reflect.code.{CtStatement, CtFieldAccess}
import spoon.reflect.declaration.{ModifierKind, CtMethod, CtClass}
import spoon.reflect.reference.{CtFieldReference, CtTypeReference}

import scala.collection.mutable

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * An implementation of [[DriverOperationsProcessor]] that generates
  * operations and related annotations for an http driver
  */
class HttpDriverOperationsProcessor[T <: HttpOperation](expConfig: BenchFlowExperiment,
                                    driver: HttpDriver,
                                    experimentId: String)(implicit env: DriversMakerEnv)
  extends DriverOperationsProcessor(expConfig, driver, experimentId)(env) {

  import HttpDriverOperationsProcessor._

  private def hardcodeHeaders(op: HttpOperation): List[CtStatement] = {

    val stmts = mutable.ListBuffer.empty[CtStatement]

    //first, declare the headers map
    stmts += getFactory.Code().createCodeSnippetStatement(
      s"java.util.Map<String, String> $headersMapName = new java.util.HashMap<String, String>()"
    )

    //then, add all headers
    op.headers.foreach { header =>

      stmts += getFactory.Code().createCodeSnippetStatement(
        s"""headers.put("${header._1}", "${header._2}")"""
      )

    }

    stmts.toList
  }

  //TODO: think about moving normalisation into BenchFlowDriver and calling it in each operation
  private def normalizedURIStmt(op: HttpOperation): CtStatement = {

    getFactory.Code.createCodeSnippetStatement(
      s"""String $normalisedEndpointName = new java.net.URI(sutEndpoint + "${op.endpoint}").normalize().toString()"""
    )

  }


  private def generateGetOperation(method: CtMethod[Void], op: HttpOperation): Unit = {

    val headersStmts = hardcodeHeaders(op)

    headersStmts.foreach { headerStmt =>
      method.getBody.addStatement(headerStmt)
      ()
    }

    method.getBody.insertEnd(normalizedURIStmt(op))

    method.getBody.insertEnd(
      getFactory.Code().createCodeSnippetStatement(
        s"""http.fetchURL($normalisedEndpointName, $headersMapName)"""
      )
    )

  }


  private def generateDeleteOperation(method: CtMethod[Void], op: HttpOperation): Unit = {

    val headersStmts = hardcodeHeaders(op)

    headersStmts.foreach { headerStmt =>
      method.getBody.addStatement(headerStmt)
      ()
    }

    method.getBody.insertEnd(normalizedURIStmt(op))

    method.getBody.insertEnd(
      getFactory.Code.createCodeSnippetStatement(
        s"""http.deleteURL($normalisedEndpointName)"""
      )
    )

  }


  private def generatePutOperation(method: CtMethod[Void], op: HttpOperation): Unit = {

    val headersStmts = hardcodeHeaders(op)

    headersStmts.foreach { headerStmt =>
      method.getBody.addStatement(headerStmt)
      ()
    }

    val contentType = op.headers.get("Content-Type") match {
      case Some(cType) => cType
      case None => "text/plain"
    }

    method.getBody.insertEnd(normalizedURIStmt(op))

    method.getBody.insertEnd(
      getFactory.Code.createCodeSnippetStatement(
        s"""http.putURL($normalisedEndpointName,
           |"${op.data}".getBytes(java.nio.charset.Charset.forName("UTF-8")),
           |"$contentType", $headersMapName)""".stripMargin
      )
    )


  }

  private def generatePostOperation(method: CtMethod[Void], op: HttpOperation): Unit = {

    val headerStmts = hardcodeHeaders(op)

    headerStmts.foreach { headerStmt =>
      method.getBody.addStatement(headerStmt)
      ()
    }

    method.getBody.insertEnd(normalizedURIStmt(op))

    method.getBody.insertEnd(
      getFactory.Code.createCodeSnippetStatement(
        op.data match {
          case Some(payload) => s"""http.fetchURL($normalisedEndpointName, "$payload", $headersMapName)"""
          case None =>   s"""http.fetchURL($normalisedEndpointName, $headersMapName)"""
        }
      )
    )
  }

  override protected def generateOperation(element: CtClass[_])(op: HttpOperation): Unit = {

    val methodName = s"do${op.name.capitalize}Request"
    val methodBody = getFactory.Core().createBlock()
    val method: CtMethod[Void] = getFactory.Method()
      .create(element,
        getFactory.Code().modifiers(ModifierKind.PUBLIC),
        getFactory.Type().VOID_PRIMITIVE,
        methodName,
        null, null, methodBody)

    method.addThrownType(getFactory.Type().createReference(classOf[java.lang.Exception]))


    op.method match {
      case Get    => generateGetOperation(method, op)
      case Post   => generatePostOperation(method, op)
      case Put    => generatePutOperation(method, op)
      case Delete => generateDeleteOperation(method, op)
    }

    val max90th = convertMax90th(GenerationDefaults.timeUnit,
      driver.configuration.flatMap(_.max90th)
        .getOrElse(GenerationDefaults.max90th))

    //add @BenchmarkOperation annotation
    val benchmarkOperationAnnotation = getFactory.Annotation().annotate(method, classOf[BenchmarkOperation])
    val benchmarkOperationName = getFactory.Code().createLiteral(op.name)
    benchmarkOperationAnnotation.addValue("name", benchmarkOperationName)
    //benchmarkOperationAnnotation.addValue("max90th", driver.configuration.flatMap(_.max90th).getOrElse(GenerationDefaults.max90th))
    benchmarkOperationAnnotation.addValue("max90th", max90th)
    benchmarkOperationAnnotation.addValue("percentileLimits",
      GenerationDefaults.percentileLimits(max90th).map(java.lang.Double.valueOf).toArray[java.lang.Double])
    val fieldRead: CtFieldAccess[Timing] = getFactory.Core().createFieldRead()
    val enumReference: CtTypeReference[Timing] = getFactory.Type().createReference(classOf[Timing])
    val fieldReference: CtFieldReference[Timing] = getFactory.Field()
      .createReference(enumReference, enumReference, Timing.AUTO.name())
    fieldReference.setStatic(true)
    fieldRead.setVariable(fieldReference)
    benchmarkOperationAnnotation.addValue("timing", fieldRead)


  }

}
object HttpDriverOperationsProcessor {

  val headersMapName = "headers"
  val normalisedEndpointName = "normalisedEndpoint"

}
