package edu.cmu.ml.rtw.pra.utils

import com.mattg.util._

/**
  *
  * @project pra
  * @author lx on 10:55 AM 05/12/2017
  */

class ImmutableConcurrentDictionary(fileUtil: FileUtil = new FileUtil)
  extends MutableConcurrentDictionary(fileUtil) {

  def getIndex(key: String, default: Int = -1): Int = {
    if (key == null) {
      throw new RuntimeException("A null key was passed to the dictionary!")
    }
    map.get(key) match {
      case Some(i) => {
        ensureReverseIsPresent(i)
        i
      }
      case None => default
    }
  }

  def readFromFile(filename: String) {
    map.clear()
    reverse_map.clear()

    val factory = new StringParser
    var max_index = 0
    for (line <- fileUtil.getLineIterator(filename)) {
      val parts = line.split("\t")
      val num = parts(0).toInt
      if (num > max_index) {
        max_index = num
      }
      val key = factory.fromString(parts(1))
      map.put(key, num)
      reverse_map.put(num, key)
    }
    nextIndex.set(max_index+1)
  }

  def readFromDict(dict: MutableConcurrentDictionary) = {
    map.clear()
    reverse_map.clear()

    val dictSeq = dict.map.toSeq
    0 until dict.map.size map { i =>
      map.put(dictSeq(i)._1, dictSeq(i)._2)
      reverse_map.put(dictSeq(i)._2, dictSeq(i)._1)
    }
    nextIndex.set(dict.nextIndex.get())
  }
}

