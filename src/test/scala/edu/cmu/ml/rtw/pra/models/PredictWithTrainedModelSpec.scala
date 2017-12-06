package edu.cmu.ml.rtw.pra.models

import edu.cmu.ml.rtw.pra.utils.ModelRunner
import org.scalatest.{FlatSpecLike, Matchers}

/**
  *
  * @project pra
  * @author lx on 6:02 PM 05/12/2017
  */

class PredictWithTrainedModelSpec extends FlatSpecLike with Matchers{


  val modelDirPath = "./examples"
  val dataDirPath = "./examples"
  val filter = "wordnet_sfe_bfs_pra_anyrel_subset"
  val runner = new ModelRunner(modelDirPath, dataDirPath, filter)

  val predictFileDir = "./examples/splits/wordnet_with_negatives"
  val resultDir = "./examples/testResult/"
  runner.runPredict(predictFileDir, resultDir)
}
