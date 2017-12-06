package edu.cmu.ml.rtw.pra.utils

import java.io.File

import com.mattg.util.{FileUtil, SpecFileReader}
import org.json4s.DefaultFormats

import scala.util.{Failure, Random, Success, Try}

/**
  *
  * @project pra
  * @author lx on 6:09 PM 05/12/2017
  */

class ModelRunner(modelDirPath: String, dataDirPath: String, filter: String) {

  implicit val formats = DefaultFormats
  val SPEC_DIR = "/experiment_specs/"
  val RESULTS_DIR = "/results/"
  val fileUtil = new FileUtil


  val model_prabase = fileUtil.addDirectorySeparatorIfNecessary(modelDirPath)
  val data_prabase = fileUtil.addDirectorySeparatorIfNecessary(dataDirPath)

  var toRunRelations: Seq[String] = Nil

  def shouldKeepFile(filters: String)(file: File): Boolean = {
    if (file.getAbsolutePath.contains(filters)) return true
    else false
  }

  def initEnv() {
    val pra_base = data_prabase
    val experiment_filters = filter
    val random = new Random
    val experiment_spec_dir = s"${pra_base}/experiment_specs/"
    val experiment_specs = fileUtil.recursiveListFiles(new File(experiment_spec_dir), """.*\.json$""".r)
    if (experiment_specs.size == 0) {
      println("No experiment specs found.  Check your base path (the first argument).")
    }
    val filtered = experiment_specs.filter(shouldKeepFile(experiment_filters))
    println(s"Found ${experiment_specs.size} experiment specs, and kept ${filtered.size} of them")
    if (filtered.size == 0) {
      println("No experiment specs kept after filtering.  Check your filters (all arguments after "
        + "the first one).")
    }
    val shuffled = random.shuffle(filtered)
    shuffled.headOption.map(initSpecEnv _)
  }

  def initSpecEnv(spec_file: File) {
    val pra_base = data_prabase
    val spec_lines = fileUtil.readLinesFromFile(spec_file)
    val params = new SpecFileReader(pra_base).readSpecFile(spec_file)
    val result_base_dir = pra_base + RESULTS_DIR
    val experiment_spec_dir = pra_base + SPEC_DIR
    println(s"Running PRA from spec file $spec_file")
    val experiment = spec_file.getAbsolutePath().split(SPEC_DIR).last.replace(".json", "")
    val result_dir = result_base_dir + experiment
//    if (new File(result_dir).exists) {
//      println(s"Result directory $result_dir already exists. Skipping...")
//      return
//    }

    ModelAction.setModelEnv(model_prabase, experiment, params, fileUtil)
    ModelAction.setDataEnv(data_prabase, experiment, params, fileUtil)
    val res = Try {
      (params \ "split" \ "relations").extract[List[String]]
    } match {
      case Success(v) => v
      case Failure(e) => throw new RuntimeException("Relations that to predict is not set")
    }

    toRunRelations = res

  }

  initEnv()

  def runPredict(predictFileDir: String, resultDir: String) = {
    toRunRelations.par.map {
      relation =>
        val resultPath = s"${resultDir}/${relation}"
        ModelAction.predict(predictFileDir, resultPath, relation)
    }
  }

}


object ModelRunner{
  def NO_main(args: Array[String]): Unit = {
    val modelDirPath = "./examples"
    val dataDirPath = "./examples"
    val filter = "wordnet_sfe_bfs_pra_anyrel_subset"
    val runner = new ModelRunner(modelDirPath, dataDirPath, filter)

    val predictFileDir = "./examples/splits/wordnet_with_negatives"
    val resultDir = "./examples/testResult/"
    runner.runPredict(predictFileDir, resultDir)
  }
}
