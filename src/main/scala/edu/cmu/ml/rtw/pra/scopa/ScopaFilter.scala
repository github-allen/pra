package edu.cmu.ml.rtw.pra.scopa

import com.mattg.util.{FakeFileUtil, FileUtil, MutableConcurrentDictionary}
import edu.cmu.ml.rtw.pra.data.NodePairInstance
import edu.cmu.ml.rtw.pra.experiments.{Outputter, RelationMetadata}
import edu.cmu.ml.rtw.pra.features.NodePairBfsPathFinder
import edu.cmu.ml.rtw.pra.features.extractors.PraFeatureExtractor
import edu.cmu.ml.rtw.pra.graphs.{Graph, GraphOnDisk}
import org.json4s.JsonAST.{JNothing, JString, JValue}

import scala.io.Source

/**
  *
  * @project scopa_pra
  * @author lx on 5:07 PM 11/12/2017
  */

//用于打标签
class ScopaFilter(outputter: Outputter = Outputter.justLogger, fileUtil: FileUtil = new FakeFileUtil) {

  def build_graph(sourceFile: String, graphDir: String): Graph = {

    val node_dict = new MutableConcurrentDictionary
    val edge_dict = new MutableConcurrentDictionary

    if(fileUtil.fileExists(graphDir)) fileUtil.deleteDirectory(graphDir)

    val edges_filepath = s"$graphDir/graph_chi/edges.tsv"
    fileUtil.mkdirsForFile(edges_filepath)

    //SOV格式
    Source.fromFile(sourceFile).getLines() foreach  {
      case str => {
        val line = str.trim
        if (line.size != 0) {
          val Array(source, target, edge, _*) = line.split("\t").map(_.trim)

          val sourceIndex = node_dict.getIndex(source)
          val targetIndex = node_dict.getIndex(target)
          val edgeIndex = edge_dict.getIndex(edge)

          val edges = Seq(sourceIndex, targetIndex, edgeIndex).mkString("\t")

          fileUtil.writeLinesToFile(edges_filepath, Seq(edges), true)

        }
      }
    }


    node_dict.writeToFile(s"$graphDir/node_dict.tsv")
    edge_dict.writeToFile(s"$graphDir/edge_dict.tsv")

    val graph = new GraphOnDisk(graphDir, outputter, fileUtil)

    graph
  }


  def get_metadata(metaDir: String): RelationMetadata = {
    new RelationMetadata(metaDir, outputter, fileUtil)
  }


  def praFilter(graph: Graph, relationMetadata: RelationMetadata, relation: String, instance: NodePairInstance) = {
    def makeFinder(params: JValue = JNothing, relation: String) =
      new NodePairBfsPathFinder(params, relation, RelationMetadata.empty, outputter, fileUtil)

    val finder = new NodePairBfsPathFinder(JNothing, relation, relationMetadata, outputter, fileUtil)
    val subgraph = finder.getSubgraphForInstance(instance)
    val extractor = new PraFeatureExtractor(JString("PraFeatureExtractor"))
    val fe = extractor.extractFeatures(instance, subgraph)

    fe.isEmpty
  }

}

object ScopaFilter {
  def main(args: Array[String]): Unit = {

    val sourceFile = s"/Users/lx/Downloads/praTest/wordNet/relation_metadata/WordNet/wordnet_edgelist_baseline.tsv"
    val graphDir = "/Users/lx/Downloads/praTest/scopa/graph"
    val datasource = new ScopaFilter

    val graph = datasource.build_graph(sourceFile, graphDir)

    val metaDir = "/Users/lx/Downloads/praTest/wordNet/relation_metadata"
    val relationMetadata = datasource.get_metadata(metaDir)


  }
}
