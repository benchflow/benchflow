package cloud.benchflow.experiment.sources.processors

import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import spoon.reflect.declaration.{CtClass, CtType, ModifierKind}
import spoon.reflect.factory.{Factory, CodeFactory}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 11/05/16.
  */
abstract class WfMSPluginLoaderProcessor(expConfig: BenchFlowExperiment,
                                experimentId: String,
                                benchmarkSourcesProcessor: ChildProcessor)(implicit env: DriversMakerEnv)
  extends BenchmarkSourcesProcessor(expConfig, experimentId)(env) {


  override protected def doProcess(element: CtClass[_]): Unit = {

    val apiType: CtType[_] = getFactory.Type().get("cloud.benchflow.libraries.wfms.WfMSApi")
    val pluginType = getFactory.Type()
      .get(s"cloud.benchflow.plugins.wfms.${expConfig.sut.name}.WfMSPlugin")

    element.addNestedType(apiType)
    element.addNestedType(pluginType)

    //I have to do this to fix the complete name of WfMSPlugin's superclass
    val nestedPluginType: CtType[_] = element.getNestedType("WfMSPlugin")
    val nestedApiType: CtType[_] = element.getNestedType("WfMSApi")

    val pluginField = getFactory.Code().createCtField("plugin", nestedApiType.getReference, "null", ModifierKind.PRIVATE)
    element.addFieldAtTop(pluginField)

//    val configureMethodBody = element.getMethod("configure").getBody
//    configureMethodBody.addStatement(
//      getFactory.Code().createCodeSnippetStatement("plugin = new WfMSPlugin(sutEndpoint)")
//    )
    println(element.getClass.getSimpleName)
    benchmarkSourcesProcessor.processWithFactory(element, getFactory())

    nestedPluginType.getSuperclass.replace(nestedApiType.getReference)

  }
}

abstract class ChildProcessor(expConfig: BenchFlowExperiment,
                              experimentId: String)(implicit env: DriversMakerEnv)
  extends BenchmarkSourcesProcessor(expConfig, experimentId) {

  def processWithFactory(element: CtClass[_], parentFactory: Factory)

}


class BenchmarkWfMSPluginLoaderProcessor(expConfig: BenchFlowExperiment,
                                         experimentId: String)(implicit env: DriversMakerEnv)
  extends WfMSPluginLoaderProcessor(expConfig,
                                    experimentId,
    new ChildProcessor(expConfig, experimentId)(env) {

      override def processWithFactory(element: CtClass[_], parentFactory: Factory): Unit = {
        val configureMethodBody = element.getMethod("configure").getBody
        configureMethodBody.addStatement(
          parentFactory.Code().createCodeSnippetStatement("plugin = new WfMSPlugin(sutEndpoint)")
        )
      }

      override protected def doProcess(element: CtClass[_]): Unit = ???
    }
  )

class DriverWfMSPluginLoaderProcessor(expConfig: BenchFlowExperiment,
                                      experimentId: String)(implicit env: DriversMakerEnv)
  extends WfMSPluginLoaderProcessor(expConfig,
    experimentId,
    new ChildProcessor(expConfig, experimentId)(env) {

      def processWithFactory(element: CtClass[_], parentFactory: Factory): Unit = {
        element.getConstructor().getBody.addStatement(
          parentFactory.Code().createCodeSnippetStatement(
            "plugin = new WfMSPlugin(sutEndpoint)"
          )
        )
      }

      override protected def doProcess(element: CtClass[_]): Unit = ???
    }
  )
