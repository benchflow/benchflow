package cloud.benchflow.test.config.test

import net.jcazevedo.moultingyaml._

import scala.util.Try

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 19/07/16.
  */
trait ValueRangeYamlProtocol extends DefaultYamlProtocol {

  implicit object ConstantYamlFormat extends YamlFormat[Constant[_]] {

    override def read(constant: YamlValue): Constant[_] = constant match {

      case YamlString(str) => Constant[String](str)
      case YamlNumber(d: Double) => Constant[Double](d)
      case YamlNumber(f: Float) => Constant[Float](f)
      case YamlNumber(i: Int) => Constant[Int](i)
      case _ => throw new DeserializationException("Unknown constant value assignment")

    }

    override def write(obj: Constant[_]): YamlValue = ???

  }

  implicit object FactorsYamlFormat extends YamlFormat[Factors[_]] {

    override def read(values: YamlValue): Factors[_] = {

      def classOfYaml(yaml: YamlValue) = yaml match {
        case YamlString(str) => classOf[String]
        case YamlNumber(i: Int) => classOf[Int]
        case YamlNumber(d: Double) => classOf[Double]
        case YamlBoolean(b) => classOf[Boolean]
        case _ => ???
      }

      values.asYamlObject.fields.get(YamlString("values")) match {

        case Some(YamlArray(vals)) =>

          val valuesType: Class[_] =
            vals.foldLeft[Class[_]](classOfYaml(vals(0)))((a: Class[_], b: YamlValue) => {

              if(a == classOf[String] || classOfYaml(b) == classOf[String]) {
                classOf[String]
              }

              if(a == classOf[Double] || classOfYaml(b) == classOf[Double]) {
                classOf[Double]
              }

              else a

            })

          if(valuesType == classOf[Int]) {
            Factors(YamlArray(vals).convertTo[Seq[Int]])
          } else if(valuesType == classOf[Double]) {
            Factors(YamlArray(vals).convertTo[Seq[Double]])
          } else if(valuesType == classOf[Boolean]) {
            Factors(YamlArray(vals).convertTo[Seq[Boolean]])
          } else {
            Factors(YamlArray(vals).convertTo[Seq[String]])
          }

        case _ => throw new DeserializationException("Unspecified factor values.")

      }

    }

    override def write(obj: Factors[_]): YamlValue = ???
  }

  implicit object StepYamlFormat extends YamlFormat[Step] {

    private val numberRegex = """([0-9]*\.?[0-9]+)""".r
    private val rangeRegex = s"$numberRegex\\s*\\.\\.\\.\\s*$numberRegex".r
    private val stepRegex = s"([\\+\\-\\*])$numberRegex".r

    override def read(yaml: YamlValue): Step = {

      val rangeString = yaml.asYamlObject.fields.get(YamlString("range")).get.convertTo[String]
      val stepString = yaml.asYamlObject.fields.get(YamlString("step")).get.convertTo[String]

      val (min, max) = rangeString match {
        case rangeRegex(from, to) => (from, to)
      }

      val (op, step) = stepString match {
        case stepRegex(o, s) => (o, s)
      }

      val parsedMin =  min.toDouble
      val parsedMax = max.toDouble
      val parsedStep = step.toDouble

      Step(
        min = parsedMin,
        max = parsedMax,
        step = parsedStep,
        stepFunction = op match {
          case "+" => implicitly[Numeric[Double]].plus
          case "*" => implicitly[Numeric[Double]].times
          case "-" => implicitly[Numeric[Double]].minus
        }
      )

    }

    override def write(obj: Step): YamlValue = ???

  }


  implicit object ValueRangeYamlFormat extends YamlFormat[ValueRange[_]] {

    override def read(yaml: YamlValue): ValueRange[_] = {

      Try(ConstantYamlFormat.read(yaml))
        .recover[ValueRange[_]]({
          case _ => FactorsYamlFormat.read(yaml)
        })
        .recover({
          case _ => StepYamlFormat.read(yaml)
        }).get
    }

    override def write(obj: ValueRange[_]): YamlValue = ???

  }

}
