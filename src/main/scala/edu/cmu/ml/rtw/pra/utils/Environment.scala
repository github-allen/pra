package edu.cmu.ml.rtw.pra.utils

import com.mattg.util.{FileUtil, MutableConcurrentDictionary}
import edu.cmu.ml.rtw.pra.data.{Instance, Split}
import edu.cmu.ml.rtw.pra.experiments.{Outputter, RelationMetadata}
import edu.cmu.ml.rtw.pra.features.FeatureGenerator
import edu.cmu.ml.rtw.pra.graphs.Graph
import edu.cmu.ml.rtw.pra.models.{BatchModel, LogisticRegressionModel}
import org.json4s.JsonAST.JValue
import org.json4s.{JNothing, JValue}

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
  *
  * @project pra
  * @author lx on 5:39 PM 05/12/2017
  */

trait Environment


//目前仅考虑trainAndTest中的模型,lr,features type只考虑subgraphs
class ModelEnv(praBase: String,
               methodName: String,
               params: JValue,
               fileUtil: FileUtil) extends Environment{

  //读取的均为已经上次构建模型的目录
  val outputter = new Outputter(params \ "output", praBase, methodName, fileUtil)

  def checkValidParameters: Unit = {
    if (praBase == null) throw new RuntimeException("please set the model path first!")
  }

  //目前只接受lr以及nodepairinstance, 只能用于预测，不能拿该model进行训练
  def loadModel[T <: Instance](relation: String): Option[LogisticRegressionModel[T]] = {
    val result = Try {
      checkValidParameters
      val relationPath = outputter.baseDir + relation + "/weights.tsv"
      val dictionary = loadFeatureDict(relation)

      val featuresAndWeights = fileUtil.mapLinesFromFile(relationPath, (line) => {
        val fields = line.split("\t")
        (fields(0), fields(1).toDouble)
      }).filter(_._2 != 0.0)
      val weights = new Array[Double](featuresAndWeights.size + dictionary.size)
      for ((feature, weight) <- featuresAndWeights) {
        val featureIndex = dictionary.getIndex(feature)
        weights(featureIndex) = weight
      }

      val model = new LogisticRegressionModel[T](JNothing, outputter)

      model.lrWeights = weights

      model
    }
    result match {
      case Success(v) => Some(v)
      case Failure(v) => None
    }
  }


  def modelInit(relation: String, model: BatchModel[_ <: Instance]): Unit = {
    if (model.isInstanceOf[LogisticRegressionModel[_ <: Instance]]) {
      loadModel(relation) match {
        case Some(m) => model.asInstanceOf[LogisticRegressionModel[_ <: Instance]].lrWeights = m.lrWeights
        case None =>
      }
    }
  }

  def saveFeatureDict(relation: String, dict: MutableConcurrentDictionary, newOutputter: Outputter) = {
    checkValidParameters

    val dictPath = newOutputter.baseDir + relation + "/featuredict.tsv"
    dict.writeToFile(dictPath)
  }

  def loadFeatureDict(relation: String): MutableConcurrentDictionary = {
    checkValidParameters

    val dictPath = outputter.baseDir + relation + "/featuredict.tsv"

    val dict = new MutableConcurrentDictionary

    Try {
      dict.setFromFile(dictPath)
    } match {
      case _ => dict
    }
  }
}


class CurrentDataEnv(praBase: String,
                     methodName: String,
                     params: JValue,
                     fileUtil: FileUtil
                    ) extends Environment {

  val outputter = new Outputter(params \ "output", praBase, methodName, fileUtil)

  val relationMetadata =
    new RelationMetadata(params \ "relation metadata", praBase, outputter, fileUtil)
  val split = Split.create(params \ "split", praBase, outputter, fileUtil)
  val graph = Graph.create(params \ "graph", praBase + "/graphs/", outputter, fileUtil)

  def generator[T<:Instance](relation: String, dict: MutableConcurrentDictionary): FeatureGenerator[T] = FeatureGenerator.create(
    params \ "operation" \ "features",
    graph,
    split.asInstanceOf[Split[T]],
    relation,
    relationMetadata,
    outputter,
    featureDict = dict,
    fileUtil = fileUtil
  )


}
