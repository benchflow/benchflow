package cloud.benchflow.test.config.experiment

import cloud.benchflow.test.config.ConfigurationYamlProtocol
import org.scalatest.{Matchers, FlatSpec}

/**
  * @author Simone D'Avico (simonedavico@gmail.com)
  *
  * Created on 18/07/16.
  */
class WorkloadMixSpec extends FlatSpec with Matchers with ConfigurationYamlProtocol {

  import net.jcazevedo.moultingyaml._
  import cloud.benchflow.test.config._

  "Matrix Workload Mix" should "parse correctly" in {

    val matrixMix =
      """
        |matrix:
        |- [ 1, 2, 3 ]
        |- [ 4, 5, 6 ]
        |
        |deviation: 5
      """.stripMargin.parseYaml.convertTo[MatrixMix]

    val parsedMatrixMix = MatrixMix(
      rows = Seq(
        MatrixMixRow(Vector(1d, 2d, 3d)),
        MatrixMixRow(Vector(4d, 5d, 6d))
      ),
      deviation = Some(5d)
    )

    matrixMix should be (parsedMatrixMix)
  }


  "Flat Workload Mix" should "parse correctly" in {

    val flatMix =
      """
        |flat: [ 33, 33, 33 ]
        |deviation: 2
      """.stripMargin.parseYaml.convertTo[FlatMix]

    val parsedFlatMix = FlatMix(
      opsMix = Vector(33d, 33d, 33d),
      deviation = Some(2d)
    )

    flatMix should be (parsedFlatMix)

  }


  "FlatSequence Workload Mix" should "parse correctly" in {

    val flatSequenceMix =
      """
        |flatSequence:
        |  sequences:
        |  - [ myOp1, myOp2 ]
        |  - [ myOp2, myOp1 ]
        |  flat: [ 50, 50 ]
        |deviation: 5
      """.stripMargin.parseYaml.convertTo[FlatSequenceMix]

    val parsedFlatSequenceMix = FlatSequenceMix(
      opsMix = Vector(50d, 50d),
      rows = Vector(
        FlatSequenceMixRow(Vector("myOp1", "myOp2")),
        FlatSequenceMixRow(Vector("myOp2", "myOp1"))
      ),
      deviation = Some(5d)
    )

    flatSequenceMix should be (parsedFlatSequenceMix)

  }


  "FixedSequence Workload Mix" should "parse correctly" in {

    val fixedSequenceMix =
      """
        |fixedSequence: [ op1, op2, op3 ]
      """.stripMargin.parseYaml.convertTo[FixedSequenceMix]

    val parsedFixedSequenceMix = FixedSequenceMix(
      sequence = Vector("op1", "op2", "op3"),
      deviation = None
    )

    fixedSequenceMix should be (parsedFixedSequenceMix)

  }

}
