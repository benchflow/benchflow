package cloud.benchflow.experiment

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 10/02/16.
  */
package object config {

  import scala.util.parsing.combinator._

  implicit class BenchFlowEnvString(val s: String) extends AnyVal {
    def findBenchFlowVars = BenchFlowVariableFinder.findIn(s)
  }

  class BenchFlowVariableParser extends RegexParsers {

    //opening delimiter for a variable, ${
    val openVar = """\$\{""".r

    //closing delimiter for a variable, }
    val closeVar = """\}""".r

    val benchFlowPrefix = "BENCHFLOW_"

    //a benchflow variable, i.e. ${BENCHFLOW_VAR_1}
    //REMINDER: double escape is for string interpolation
    val benchflowVar = openVar ~> s"""$benchFlowPrefix([^\\}])+""".r <~ closeVar

    //a string without benchflow vars to resolve, i.e
    //"foobar", or "${foobar}"
    //REMINDER: the double $ is an escaped $
    val string = s"""((?!\\$$\\{$benchFlowPrefix).)*""".r

    //an expression containing benchflow variables
    val exprWithVars = ((string?) ~> benchflowVar <~ (string?))+

  }

  object BenchFlowVariableFinder extends BenchFlowVariableParser { parser =>
    def findIn(s: String) = parseAll(exprWithVars, s) match {
      case parser.NoSuccess(_, _) => None
      case parser.Success(result, _) => Some(result)
    }
  }


}
