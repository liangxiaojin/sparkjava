package liang.service;

import com.lianjia.aisearch.featurefu.expr.Expr;
import com.lianjia.aisearch.featurefu.expr.Expression;
import com.lianjia.aisearch.featurefu.expr.VariableRegistry;
import liang.bean.Feature;
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
        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        final Map<Integer,String> variables = feature.getVariables();
        final String formula = feature.getFormula();

        final List<Integer> paramOrder = new ArrayList<Integer>(variables.keySet());
        Collections.sort(paramOrder);

        List<Map<String,Object>> sources = feature.getSources();
        Map<String,JavaPairRDD> rddMap = new HashMap<String, JavaPairRDD>();
        for (Map<String,Object> source : sources){
            getSource(javaSparkContext,source,rddMap);
        }

        JavaPairRDD allRdd = rddMap.get(variables.get(paramOrder.get(0)));
        for (int i=paramOrder.get(0);i<paramOrder.size()-1;i++){
            allRdd = allRdd.join(rddMap.get(variables.get(paramOrder.get(paramOrder.get(i)+1))));
        }


        JavaPairRDD<String,Double> ll = allRdd.mapValues(new Function<Tuple2<String,String>, Double>() {
            public Double call(Tuple2<String,String> tuple2) throws Exception {
                String tuple =  tuple2.toString();
                String[] columns = tuple.replace("(","").replace(")","").split(",");
                VariableRegistry variableRegistry = new VariableRegistry();
                Expr expression = Expression.parse(formula, variableRegistry);
                for(Integer order : paramOrder){
                    variableRegistry.findVariable(variables.get(paramOrder.get(paramOrder.get(order)))).setValue(Integer.valueOf(columns[order]));
                }
                return expression.evaluate();
            }
        });
        System.out.println(ll.collectAsMap());


    }

    private static void getSource(JavaSparkContext javaSparkContext,Map<String,Object> source,
                           Map<String,JavaPairRDD> rddMap){

        Map<String,Map<String,Object>> source_1_variables =(Map<String,Map<String,Object>>) source.get("variables");
        List<String> keySet = new ArrayList<String>(source_1_variables.keySet());
        for (final String key : keySet){
             getPariRDD(key,source_1_variables,javaSparkContext,source,rddMap);
        }

    }


    private static void getPariRDD(String key,Map<String,Map<String,Object>> source_1_variables,
                                                  JavaSparkContext javaSparkContext,Map<String,Object> source,
                                                  Map<String,JavaPairRDD> rddMap){
        String path = source.get("path").toString();
        final String columnSplitSymbol = source.get("columnSplitSymbol").toString();
        final int num = (Integer) source_1_variables.get(key).get("num");

        JavaRDD<String> data = javaSparkContext.textFile(path);
        JavaRDD<String> lines =  data.flatMap(new FlatMapFunction<String, String>() {
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\r\\n")).iterator();
            }
        });

        JavaPairRDD<String,String> x = lines.mapToPair(new PairFunction<String, String, String>() {
            int i=0;
            public Tuple2<String, String> call(String s) throws Exception {
                i++;
                String[] columns = s.split(columnSplitSymbol);
                return new Tuple2<String, String>(i+"",columns[num]);
            }
        });
        rddMap.put(key,x);
    }



}
