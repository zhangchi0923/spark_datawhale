# spark_datawhale
### SparkSQL

- 从Shark（Hive on Spark）转变而来
  - 优化完全依赖Hive
  - Spark兼容Hive时存在线程安全问题

![arch](https://github.com/zhangchi0923/spark_datawhale/blob/master/images/arch.gif)

- SparkSQL
  - 融合多数据源（结构化、非结构化）
  - 机器学习算法处理数据能力
  - 最终，提供了DataFrame API

![sparksql](https://github.com/zhangchi0923/spark_datawhale/blob/master/images/sparksql.png)



- DataFrame与RDD

  - DataFrame的创建

    - 外部数据源

    ```java
      Dataset<Person> df = SparkSession.read().json("examples/src/main/resources/people.json")
    ```

      

    - 现有RDD
    - Hive表

  - DataFrame基本操作

    - DataFrame相当于Dataset of Rows，所以二者的方法是一样的

    - ```java
      df.printSchema()
      ```

    - ```java
      df.select(col("name"),col("age").plus(1)).show()
      ```

  - RDD向DataFrame转换

  - RDD相关

- RDD（Resilient Distributed Dataset）

  - 抽象的数据架构，只读的分区记录集合，不同分区分布在不同节点中
  - 迭代式算法会重用中间计算结果，I/O开销太大，RDD是一种共享内存模型
  - Action、Transformation：
    - RDD读入外部数据
    - 经过一系列Transformation每一次都会产生不同的RDD
    - 最后一个RDD执行Action，输出到外部，以上过程称为Lineage，是DAG拓扑排序的结果
  - 窄依赖：一个父RDD分区对应一个子RDD分区、或多个父RDD分区对应一个子RDD分区
  - 宽依赖：一个父RDD分区对应多个子RDD分区

  ![宽窄依赖]([https://github.com/zhangchi0923/spark_datawhale/blob/master/images/%E5%AE%BD%E7%AA%84%E4%BE%9D%E8%B5%96.png](https://github.com/zhangchi0923/spark_datawhale/blob/master/images/宽窄依赖.png))

  

  

  - Stage的划分：
    - Spark分析各个RDD之间的依赖关系生成DAG
    - DAG反向解析，遇到**宽依赖**就断开；遇到**窄依赖**就把当前的RDD加入到stage中
    - 尽量将窄依赖划分到同一stage中，可实现流水式计算

  

  ![stage](https://github.com/zhangchi0923/spark_datawhale/blob/master/images/stage.png)

  

  - RDD运行原理总结：
    - 创建RDD，并经过一系列的转化，生成各个不同的RDD
    - SparkContext分析RDD之间的依赖关系生成DAG
    - DAGScheduler将DAG解析成不同的stage，每个stage包含不同的task（task之间无shuffle关系）
    - 每个task被TaskScheduler分发给各个WorkerNode上的Executor进程去执行

  ![RDD](https://github.com/zhangchi0923/spark_datawhale/blob/master/images/RDD.png)

​	

***了解了RDD相关概念后可以认识整个Spark的架构***

- Spark架构设计

  - Application

  - Driver

  - RDD

  - DAG、DAG Scheduler

  - Job

    - stage
      - task、taskScheduler

    ![Application内部]([https://github.com/zhangchi0923/spark_datawhale/blob/master/images/Application%E5%86%85%E9%83%A8.png](https://github.com/zhangchi0923/spark_datawhale/blob/master/images/Application内部.png))

  - ClusterManager（独立、yarn、mesos）

  - WorkerNode

    - Executor进程
      - 多线程执行taskScheduler提交上来的任务

![spark架构]([https://github.com/zhangchi0923/spark_datawhale/blob/master/images/spark%E6%9E%B6%E6%9E%84.png](https://github.com/zhangchi0923/spark_datawhale/blob/master/images/spark架构.png))

#### RDD编程

- RDD创建

  - 从文件系统中读取

  ```scala
  val lines2 = sc.textFile("file:///D:/spark-2.4.3-bin-hadoop2.6/data.txt")
  ```

  - 通过并行集合（数组）创建

  ```scala
  val arr = Array(1,2,3,4,5)
  val rdd = sc.parallelize(arr)
  ```

- RDD操作

  - 转换操作（Transformation）
    - filter(func() )：筛选出满足func函数的元素，返回一个新的数据集
    - map(func() )：将每个元素传到func中，并返回一个新数据集
    - flatMap(func() )：每个输入元素可以映射到0或多个输出结果
    - groupByKey()：输入为<K,V>数据集，输出一个新的<K,Iterable>数据集
    - reduceByKey(func() )：输入为<K,V>数据集，输出为新的<K,V>数据集，V为将K传入func中进行聚合的结果
  - 动作操作（Action）
    - count()：返回数据集元素个数
    - collect()：以数组的形式返回数据集中所有元素
    - first()：返回数据集中第一个元素
    - take(n)：以数组的形式返回数据集前n个元素
    - reduce(func)：对func输入两个参数，返回一个值
    - foreach(func)：循环将数据集中每个元素传入func

- 惰性机制

  - RDD的Transformation操作并不会真正执行，直到Action操作发生，才会触发真正的计算
  - Spark的计算此时在各RDD的各个节点上进行，最后将结果返回到Driver
  - 这种惰性机制意味着Action发生时会从血缘关系的最开始触发计算
  - 对一些复用率高的中间结果（例如某一步的Transformation操作结果）可以将某一中间结果标记为**持久化**
    - persist(MEMORY_ONLY)：将RDD作为反序列化的对象保存在JVM中，若内存不够，则按照LRU原则替换缓存中的内容
    - persist(MEMORY_AND DISK)：将RDD作为反序列化的对象保存在JVM中，若内存不够，则存储在硬盘中
    - cache()：会自动调用persist(MEMORY_ONLY)
    - unpersist()：手动将RDD从缓存中移除
