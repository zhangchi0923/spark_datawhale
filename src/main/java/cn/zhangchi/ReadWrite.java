package cn.zhangchi;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class ReadWrite {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName("parquetRW")
                .setMaster("local");

        SparkContext sc = new SparkContext(conf);
        SparkSession spark = new SparkSession(sc);

//        String path = "D:\\spark-2.4.3-bin-hadoop2.6\\spark-2.4.3-bin-hadoop2.6\\examples\\src\\main\\resources\\people.json";
        Dataset<Row> peopleDF = spark.read().json("hdfs://localhost:9000/people.json");
        peopleDF.show();
        peopleDF.write().parquet("./people.parquet");
    }
}
