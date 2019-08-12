# spark_datawhale

### 数据读写

- txt文件

```scala
val conf = SparkConf().setAppName("read textFile").setMaster("local")
val sc = SparkContext(conf)
val textFile = sc.textFile("/home/data.txt")
```

- parquet文件

```scala
import spark.implicits._

val peopleDF = spark.read.json("examples/src/main/resources/people.json")
peopleDF.write.parquet("people.parquet")
val parquetFileDF = spark.read.parquet("people.parquet")
parquetFileDF.createOrReplaceTempView("parquetFile")
val namesDF = spark.sql("SELECT name FROM parquetFile WHERE age BETWEEN 13 AND 19")
namesDF.map(attributes => "Name: " + attributes(0)).show()

```

