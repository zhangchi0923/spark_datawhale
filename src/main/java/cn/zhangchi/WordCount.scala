package cn.zhangchi

import org.apache.spark.{SparkConf, SparkContext}

object WordCount {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("Word Count")
      .setMaster("local")

    val sc = new SparkContext(conf)

    val lines = sc.textFile("hdfs://localhost:9000/wc/word.txt")
    val counts = lines
      .flatMap(line => line.split(" "))
      .map(word => (word,1))
      .reduceByKey(_+_)
    counts.saveAsTextFile("hdfs://localhost:9000/wc/output")
  }

}
