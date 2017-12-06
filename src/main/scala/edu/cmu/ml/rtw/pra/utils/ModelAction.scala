package edu.cmu.ml.rtw.pra.utils

import com.mattg.util.FileUtil
import edu.cmu.ml.rtw.pra.data.{Dataset, Instance}
import org.json4s.JsonAST.JValue

/**
  *
  * @project pra
  * @author lx on 4:11 PM 04/12/2017
  */


object ModelAction {
  var modelInfo: ModelEnv = null
  var dataInfo: CurrentDataEnv = null


  def setModelEnv(praBase: String,
                  methodName: String,
                  params: JValue,
                  fileUtil: FileUtil) = {
    modelInfo = new ModelEnv(praBase, methodName, params, fileUtil)
  }

  def setDataEnv(praBase: String,
                 methodName: String,
                 params: JValue,
                 fileUtil: FileUtil) = {
    dataInfo = new CurrentDataEnv(praBase, methodName, params, fileUtil)
  }


  def predict[T <: Instance](dataDir: String,
                             resultDir: String,
                             relation: String,
                             fileUtil: FileUtil = new FileUtil
                            ) = {

    if (dataInfo == null || modelInfo == null) {
      throw new RuntimeException("Please set the modelEnv or dataEnv!")
    }

    val dataPath = fileUtil.addDirectorySeparatorIfNecessary(dataDir) + relation + "/testing.tsv"
    val resultPath = fileUtil.addDirectorySeparatorIfNecessary(resultDir) + relation + "/result.tsv"
    val dataset = dataInfo.split.readDatasetFile(dataPath, dataInfo.graph).asInstanceOf[Dataset[T]]
    val model = modelInfo.loadModel(relation)

    val scores: Seq[(T, Double)] = model match {
      case Some(m) => {
        val generator = dataInfo.generator[T](relation, modelInfo.loadFeatureDict(relation))
        val featureMatrix = generator.createTestMatrix(dataset)
        m.classifyInstances(featureMatrix)
      }
      case None => throw new RuntimeException("No trained model in local!")
    }

//    scores.sortBy(_._2)(Ordering[Double].reverse).map(println)

    fileUtil.mkdirsForFile(resultPath)
    dataInfo.outputter.outputScores(scores, dataset, resultPath)

  }
}