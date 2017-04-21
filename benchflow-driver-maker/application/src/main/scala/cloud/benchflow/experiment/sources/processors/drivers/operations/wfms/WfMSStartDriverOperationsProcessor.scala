package cloud.benchflow.experiment.sources.processors.drivers.operations.wfms

import cloud.benchflow.experiment.GenerationDefaults
import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.test.config.sut.wfms.WfMSStartDriver
import cloud.benchflow.test.config.sut.wfms.WfMSOperation
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import com.sun.faban.driver.{Timing, BenchmarkOperation}
import org.apache.commons.io.FilenameUtils
import spoon.reflect.code.{CtIf, CtFieldAccess, CtCodeSnippetExpression}
import spoon.reflect.declaration._
import spoon.reflect.reference.{CtFieldReference, CtTypeReference}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * An implementation of [[WfMSDriverOperationsProcessor]] that generates
  * operations and related annotations for a wfms benchmark
  */
class WfMSStartDriverOperationsProcessor[T <: WfMSOperation](expConfig: BenchFlowExperiment,
                                                             driver: WfMSStartDriver,
                                                             experimentId: String)(implicit env: DriversMakerEnv)
  extends WfMSDriverOperationsProcessor(expConfig, driver, experimentId)(env)  {

    override protected def generateOperation(e: CtClass[_])(op: WfMSOperation): Unit = {

      val methodName = s"do${FilenameUtils.removeExtension(op.name).capitalize}Request"
      val methodBody = getFactory.Core().createBlock()
      val method: CtMethod[Void] = getFactory.Method()
        .create(e,
          getFactory.Code().modifiers(ModifierKind.PUBLIC),
          getFactory.Type().VOID_PRIMITIVE,
          methodName,
          null, null, methodBody)

      method.addThrownType(getFactory.Type().createReference(classOf[java.lang.Exception]))

      val isStartedCheck: CtCodeSnippetExpression[java.lang.Boolean] = getFactory.Code().createCodeSnippetExpression("isStarted()")

      val processData = op.data.map(d => s"$d").getOrElse("null")
      val pluginCall = getFactory.Code().createCodeSnippetStatement(s"""plugin.startProcessInstance("${op.name}", $processData)""")
      val mockCall = getFactory.Code().createCodeSnippetStatement("""plugin.startProcessInstance("mock.bpmn", null)""")

      val ifStatement: CtIf = getFactory.Core().createIf()
      methodBody.addStatement(ifStatement
        .setCondition[CtIf](isStartedCheck)
        .setThenStatement[CtIf](pluginCall)
        .setElseStatement[CtIf](mockCall))

      val max90th = convertMax90th(GenerationDefaults.timeUnit,
                                   driver.configuration.flatMap(_.max90th)
                                         .getOrElse(GenerationDefaults.max90th))

      //TODO: think of a way to extract this in the abstract OperationsProcessor
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
