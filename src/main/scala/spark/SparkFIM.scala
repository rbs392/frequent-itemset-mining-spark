package spark

import experiments.Runner
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import sequential.Apriori.Itemset
import sequential.{FIM, Util}
import sequential.Util.absoluteSupport


trait SparkFIM extends FIM {

  /**
    * Implemented by YAFIM, RApriori or DFPS
    */
  def findFrequentItemsets(transactions: RDD[Itemset], singletons: RDD[(String, Int)], minSupport: Int,
                           spark: SparkSession, sc: SparkContext): List[Itemset]

  /**
    * Common method for all Spark FIM implementations.
    * Generates a transaction and singletons RDD as well as calculate minimum support from a percentage.
    */
  override def findFrequentItemsets(fileName: String, separator: String, transactions: List[Itemset], minSupport: Double): List[Itemset] = {
    var spark: SparkSession = null
    if (!Runner.clusterMode) {
      spark = SparkSession.builder()
        .appName("FIM")
        .master("local[4]")
        //.config("spark.eventLog.enabled", "true")
        .getOrCreate()
    }
    else {
      spark = SparkSession.builder().getOrCreate()
    }

    val sc = spark.sparkContext
    sc.setLogLevel("WARN")
    val t0 = System.currentTimeMillis()

    var transactionsRDD: RDD[Itemset] = null
    var support: Int = 0

    if (!fileName.isEmpty) {
      // Fetch transaction
      val file = List.fill(Util.replicateNTimes)(fileName).mkString(",")
      transactionsRDD = sc.textFile(file, Util.minPartitions)
        .filter(!_.trim.isEmpty)
        .map(_.split(separator + "+"))
        .map(l => l.map(_.trim).toList)
        .cache()
      support = absoluteSupport(minSupport, transactionsRDD.count().toInt)
    }
    else {
      transactionsRDD = sc.parallelize(transactions)
      support = absoluteSupport(minSupport, transactions.size)
    }

    // Generate singletons
    val singletonsRDD = transactionsRDD
      .flatMap(identity)
      .map(item => (item, 1))
      .reduceByKey(_ + _)
      .filter(_._2 >= support)

    val frequentItemsets = findFrequentItemsets(transactionsRDD, singletonsRDD, support, spark, sc)

    executionTime = System.currentTimeMillis() - t0
    frequentItemsets
  }

}
