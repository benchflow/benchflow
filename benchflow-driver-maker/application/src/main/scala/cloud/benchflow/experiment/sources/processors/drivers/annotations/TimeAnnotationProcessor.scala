package cloud.benchflow.experiment.sources.processors.drivers.annotations

import cloud.benchflow.experiment.sources.processors.BenchmarkSourcesProcessor
import cloud.benchflow.driversmaker.utils.env.DriversMakerEnv
import cloud.benchflow.test.config.experiment.BenchFlowExperiment
import com.sun.faban.driver.{NegativeExponential, CycleType, FixedTime}
import spoon.reflect.code.CtFieldAccess
import spoon.reflect.declaration.CtClass
import spoon.reflect.reference.{CtFieldReference, CtTypeReference}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 01/05/16.
  */
class TimeAnnotationProcessor(expConfig: BenchFlowExperiment,
                              experimentId: String)(implicit env: DriversMakerEnv)
  extends BenchmarkSourcesProcessor(expConfig, experimentId)(env) {

  override def doProcess(e: CtClass[_]): Unit = {

    //TODO: implement all other time types (NegativeExponential, ...)
    val timeAnnotation = getFactory.Annotation().annotate(e, classOf[FixedTime])
    timeAnnotation.addValue("cycleTime", 1000)
    timeAnnotation.addValue("cycleDeviation", 5)

    val fieldRead: CtFieldAccess[CycleType] = getFactory.Core().createFieldRead()
    val enumReference: CtTypeReference[CycleType] = getFactory.Type().createReference(classOf[CycleType])
    val fieldReference: CtFieldReference[CycleType] = getFactory.Field()
      .createReference(enumReference, enumReference, CycleType.THINKTIME.name())
    fieldReference.setStatic(true)
    fieldRead.setVariable(fieldReference)
    timeAnnotation.addValue("cycleType", fieldRead)

  }
}
