package liang;

import com.lianjia.aisearch.featurefu.expr.Expr;
import com.lianjia.aisearch.featurefu.expr.Expression;
import com.lianjia.aisearch.featurefu.expr.VariableRegistry;
import liang.bean.Features;
import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.ho.yaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;

public class Test1 implements Serializable {
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
        File dumpFile = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\featureConfig.yaml");
        Features features = null;
        try {
            features = Yaml.loadType(dumpFile, Features.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (Object o : features.getFeatures()) {
            final Map<String, Object> feature = (Map) o;
            //sparkContext.setLogLevel("INFO");
            JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);
            // Create the DataFrame

            JavaRDD<String> data = javaSparkContext.textFile(feature.get("path").toString());

            JavaRDD<String> lines =  data.flatMap(new FlatMapFunction<String, String>() {
                public Iterator<String> call(String s) throws Exception {
                    return Arrays.asList(s.split("\\r\\n")).iterator();
                }
            });

            JavaRDD<List<String>> missingResult =  lines.map(new Function<String,List<String>>() {
                public List<String> call(String s) throws Exception {
                    String[] columns = s.split("\\s");
                    for (int i =0 ;i<columns.length;i++){
                        if (StringUtils.isBlank(columns[i])){
                            columns[i] = "4";
                        }
                    }
                    return Arrays.asList(columns);
                }
            });

            System.out.println("missingResult:"+missingResult.collect());


            JavaRDD<Double> results =  missingResult.map(new Function<List<String>,Double>() {
                public Double call(List<String> s) throws Exception {
                    System.out.println(s);
                    return Expression.evaluate(feature.get("formula").toString(),s);
                }
            });

            System.out.println(results.collect());
            //results:[0.9933071490757153, 0.999999999994891]
        }
    }
}
