package liang.service;

import com.lianjia.aisearch.featurefu.expr.Expression;
import liang.bean.Feature;
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

public class DistributeHandler implements Serializable {
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
        File dumpFile = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\distribute.yaml");
        Feature feature = null;
        try {
            feature = Yaml.loadType(dumpFile, Feature.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        List<Double> results = DistributeHandler.getFeature(feature);

        System.out.println(results);
    }


    private static List<Double> getFeature(Feature feature){
        String symbol =  feature.getSymbol();
        List<Map<String,Object>> params = feature.getParams();
        return params(params,symbol);

    }

     private static  List<Double> params(List<Map<String,Object>> params, final String symbol){

        List<String> a = new ArrayList<String>();
        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        JavaRDD<String> paramRdds = javaSparkContext.textFile("D:\\code\\ll.txt");

        for (Map<String,Object> param : params){
            Object symbol1 =  param.get("symbol");

            if (null == symbol1){
                JavaSparkContext javaSparkContext1 = new JavaSparkContext(sparkContext);
                JavaRDD<String> data = javaSparkContext1.textFile(param.get("path").toString());
                JavaRDD<String> lines =  data.flatMap(new FlatMapFunction<String, String>() {
                    public Iterator<String> call(String s) throws Exception {
                        return Arrays.asList(s.split("\\r\\n")).iterator();
                    }
                });
                JavaRDD<String> results =  lines.map(new Function<String,String>() {
                    public String call(String s) throws Exception {
                        System.out.println(s);
                        String[] columns = s.split("\\s");
                        return columns[0];
                    }
                });
                System.out.println(" results:"+results.collect());
                paramRdds.union(results);
                System.out.println(" union : "+paramRdds.collect());

            }else {
                List<Map<String,Object>> params1 = (List<Map<String,Object>>)param.get("params");
                String paramSymbol = param.get("symbol").toString();
                params(params1,paramSymbol);
            }
        }

        JavaRDD<Double> results =  paramRdds.map(new Function<String, Double>() {
            public Double call(String a) throws Exception {
                String[] columns =  a.split("\\s");
                return Expression.evaluate("("+symbol+" "+columns[0]+" "+columns[1]+")");
            }
        });
        return results.collect();
    }

}
