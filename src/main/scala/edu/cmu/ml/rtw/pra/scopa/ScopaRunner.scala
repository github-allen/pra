package edu.cmu.ml.rtw.pra.scopa

import com.mattg.util.FileUtil
import edu.cmu.ml.rtw.pra.experiments.{Driver, ExperimentScorer}
import org.json4s._
import org.json4s.native.JsonMethods.parse

/**
  *
  * @project scopa-pra
  * @author lx on 10:52 AM 08/12/2017
  */

class ScopaRunner {
  val fileUtil = new FileUtil
  var BASE_DIR = "/Users/lx/Downloads/praTest/wordNet/"
  var RESULTS_DIR = "/results/"
  var params = getParams()

  var methodName = "praInfer"

  //TODO use in future
  def getParamsFromFile(filePath: String, fileUtil: FileUtil = fileUtil) = {
    val content = fileUtil.readFileContents(filePath)

    parse(content)
  }

  def getParams(filePath: String = s"${BASE_DIR}relation_metadata/WordNet/wordnet_edgelist_baseline.tsv"): JValue = {
    parse(
      s"""{
         |  "graph": {
         |    "name": "WordNet",
         |    "relation sets": [
         |      {
         |        "is kb": true,
         |        "relation file": "$filePath"
         |      },
         |    ]
         |  },
         |  "relation metadata": "WordNet",
         |   "split": {
         |    "name": "wordnet_with_negatives",
         |    "relation metadata": "WordNet",
         |    "graph": "WordNet",
         |    "percent training":0.8,
         |    "relations":["_part_of"],
         |    "negative instances": {
         |      "negative to positive ratio": 4
         |    }
         |  },
         |  "operation": {
         |    "features": {
         |      "type": "subgraphs",
         |      "path finder": {
         |        "type": "BfsPathFinder",
         |        "number of steps": 2
         |      },
         |      "feature extractors": [
         |        "PraFeatureExtractor",
         |        "AnyRelFeatureExtractor"
         |      ],
         |      "feature size": -1
         |    }
         |    "learning": {
         |      "l1 weight": 0.5,
         |      "l2 weight": 0.01
         |    }
         |  },
         |  "output":{
         |  "output index matrices": true
         |  }
         |}
    """.stripMargin
    )
  }


  def initEnv(pra_base: String, result_dir: String, kb_path: String, method_name: String = "praInfer") = {
    BASE_DIR = fileUtil.addDirectorySeparatorIfNecessary(pra_base)
    RESULTS_DIR = fileUtil.addDirectorySeparatorIfNecessary(result_dir)
    methodName = methodName
    params = getParams(kb_path)
  }


  def run() = {

    val resultPath = BASE_DIR + RESULTS_DIR
    if(fileUtil.fileExists(resultPath)){
      fileUtil.deleteDirectory(resultPath)
    }
    new Driver(BASE_DIR, methodName, params, fileUtil).runPipeline()
  }


  def score(): Unit ={
    ExperimentScorer.scoreExperiments(BASE_DIR)
  }

}


object ScopaRunner {

  def main(args: Array[String]): Unit = {
    val runner = new ScopaRunner

//    runner.run
    runner.score
  }
}