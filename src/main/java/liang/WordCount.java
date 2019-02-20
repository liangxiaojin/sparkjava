package liang;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class WordCount {

    public static void main(String[] args) {
        // 第一步：创建SparkConf对象,设置相关配置信息
        SparkConf conf = new SparkConf();
        conf.setAppName("wordcount");
        conf.setMaster("local");

        // 第二步：创建JavaSparkContext对象，SparkContext是Spark的所有功能的入口
        JavaSparkContext sc = new JavaSparkContext(conf);

//        // 第三步：创建一个初始的RDD
//        // SparkContext中，用于根据文件类型的输入源创建RDD的方法，叫做textFile()方法
//        JavaRDD<String> lines = sc.textFile("D:\\code\\people.txt");
//
//        // 第四步：对初始的RDD进行transformation操作，也就是一些计算操作
//        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
//
//            private static final long serialVersionUID = 1L;
//
//            public Iterator<String> call(String line) throws Exception {
//
//                return Arrays.asList(line.split(",")).iterator();
//
//            }
//        });
//        System.out.println("split:" + words.collect());
//        JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
//            public Tuple2<String, Integer> call(String word) throws Exception {
//                return new Tuple2<String, Integer>(word, 1);
//            }
//        });
//        System.out.println("pair:" + pairs.collect());
//
//
//        JavaPairRDD<String, Integer> wordCounts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
//            private static final long serialVersionUID = 1L;
//
//            public Integer call(Integer v1, Integer v2) throws Exception {
//                return v1 + v2;
//
//            }
//        });
//        System.out.println("count:" + wordCounts.collect());
//
//        wordCounts.foreach(new VoidFunction<Tuple2<String, Integer>>() {
//
//            private static final long serialVersionUID = 1L;
//
//            public void call(Tuple2<String, Integer> wordCount) throws Exception {
//                System.out.println(wordCount._1 + "------" + wordCount._2 + "times.");
//            }
//        });
//
//
//        List<Integer> data = Arrays.asList(5, 1, 1, 4, 4, 2, 2);
//
//        JavaRDD<Integer> javaRDD = sc.parallelize(data, 2);
//        System.out.println("parallelize numSlices:" + javaRDD.collect());
//
//        Integer reduceRDD = javaRDD.reduce(new Function2<Integer, Integer, Integer>() {
//
//            public Integer call(Integer v1, Integer v2) throws Exception {
//                return v1 * v2;
//            }
//        });
//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + reduceRDD);


        List<Integer> data1 = Arrays.asList(5, 1, 3, 4, 4,4, 2, 2);
        JavaRDD<Integer> javaRDD1 = sc.parallelize(data1,3);
        JavaPairRDD<Integer, Integer> javaPairRDD = javaRDD1.mapToPair(new PairFunction<Integer, Integer, Integer>() {
            int i = 0;

            public Tuple2<Integer, Integer> call(Integer integer) throws Exception {
                System.out.println("1i="+i+" integer="+integer);
                i++;
                System.out.println("2i="+i+" integer="+integer);
                return new Tuple2<Integer, Integer>(integer, i + integer);
            }
        });
        System.out.println(javaPairRDD.collect());
        System.out.println("lookup------------" + javaPairRDD.lookup(3));
        sc.close();
    }
}
