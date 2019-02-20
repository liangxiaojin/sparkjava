package liang.service;

import com.lianjia.aisearch.featurefu.expr.Expr;
import com.lianjia.aisearch.featurefu.expr.Expression;
import com.lianjia.aisearch.featurefu.expr.VariableRegistry;
import liang.bean.Feature;
import liang.bean.Features;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.ho.yaml.Yaml;
import scala.Tuple2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class EntiretyHandler {
    static transient SparkContext sparkContext;
    private final static transient SparkConf SPARK_CONF = new SparkConf();

    public static void main(String[] args) {
        SPARK_CONF.setMaster("local");
        SPARK_CONF.setAppName("liang");
        SPARK_CONF.set("spark.hadoop.validateOutputSpecs", "false");
        SPARK_CONF.set("spark.akka.frameSize", "300");
        SPARK_CONF.set("spark.driver.maxResultSize", "6g");
        SPARK_CONF.setAppName("SugLog");
        sparkContext = new SparkContext(SPARK_CONF);
        File dumpFile = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\entirety.yaml");
        Feature feature = null;
        try {
            feature = Yaml.loadType(dumpFile, Feature.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        //results:[0.9933071490757153, 0.999999999994891]
    }

}
