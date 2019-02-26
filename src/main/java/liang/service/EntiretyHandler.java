package liang.service;

import com.lianjia.aisearch.featurefu.expr.Expr;
import com.lianjia.aisearch.featurefu.expr.Expression;
import com.lianjia.aisearch.featurefu.expr.VariableRegistry;
import liang.bean.Feature;
import org.apache.commons.lang.StringUtils;
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
import scala.util.matching.Regex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;

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
        //读取yaml文件
        File dumpFile = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\entirety.yaml");
        Feature feature = null;
        try {
            feature = Yaml.loadType(dumpFile, Feature.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);
        //获取yaml中的公式，公式中的变量
        final String formula = feature.getFormula();
        Map<Integer,String> variableMap = new HashMap<Integer, String>();
        List<String> variables = new ArrayList<String>();
        Expression.variables(formula,variables);

        //公式变量index及变量的map
        for (int i= 0;i<variables.size();i++){
            variableMap.put(i,variables.get(i));
        }

        final List<Integer> paramOrder = new ArrayList<Integer>(variableMap.keySet());

        //通过数据源获取需要的数据，放入rddMap，key是变量名，value是具体列值
        List<Map<String,Object>> sources = feature.getSources();
        Map<String,JavaPairRDD> rddMap = new HashMap<String, JavaPairRDD>();
        for (Map<String,Object> source : sources){
            getSource(javaSparkContext,source,rddMap);
        }

        //按变量顺序join rdd
        JavaPairRDD allRdd = rddMap.get(variableMap.get(paramOrder.get(0)));
        for (int i=1;i<paramOrder.size();i++){
            System.out.println(variableMap.get(paramOrder.get(i)));
            allRdd = allRdd.join(rddMap.get(variableMap.get(paramOrder.get(i))));
        }

        System.out.println("allRdd:"+allRdd.collectAsMap());

        //放入公式计算
        JavaPairRDD<String,Double> ll = allRdd.mapValues(new Function<Tuple2<String,String>, Double>() {
            public Double call(Tuple2<String,String> tuple2) throws Exception {
                String tuple =  tuple2.toString();
                String[] columns = tuple.replace("(","").replace(")","").split(",");
                return Expression.evaluate(formula,Arrays.asList(columns));
            }
        });
        System.out.println(ll.collectAsMap());


    }

    private static void getSource(JavaSparkContext javaSparkContext,Map<String,Object> source,
                           Map<String,JavaPairRDD> rddMap){

        Map<String,Map<String,Object>> source_1_variables =(Map<String,Map<String,Object>>) source.get("variables");
        List<String> keySet = new ArrayList<String>(source_1_variables.keySet());
        for (final String key : keySet){
            //key: x b a
             getPariRDD(key,source_1_variables,javaSparkContext,source,rddMap);
        }

    }


    private static void getPariRDD(String key,Map<String,Map<String,Object>> source_1_variables,
                                                  JavaSparkContext javaSparkContext,Map<String,Object> source,
                                                  Map<String,JavaPairRDD> rddMap){
        String path = source.get("path").toString();
        final String columnSplitSymbol = source.get("columnSplitSymbol").toString();
        final int num = (Integer) source_1_variables.get(key).get("num");
        final Map<String,Object> missing = (Map<String,Object>) source_1_variables.get(key).get("missing");

        JavaRDD<String> data = javaSparkContext.textFile(path);
        JavaRDD<String> lines =  data.flatMap(new FlatMapFunction<String, String>() {
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\r\\n")).iterator();
            }
        });
        JavaRDD<List<String>> missingRDD = lines.map(new Function<String, List<String>>() {
            public List<String> call(String s) throws Exception {
                String[] columns = s.split(columnSplitSymbol);
                if (null != missing){
                    String missingWay =(String) missing.get("way");
                    Double value = (Double)  missing.get("value");
                    for (int i =0 ;i<columns.length;i++){
                        if (StringUtils.isBlank(columns[i])){
                            if ("assign".equals(missingWay)){
                                columns[i] = value+"";
                            }
                        }
                    }
                }
                return Arrays.asList(columns);
            }
        });


        JavaPairRDD<String,String> x = missingRDD.mapToPair(new PairFunction<List<String>, String, String>() {
            int i=0;
            public Tuple2<String, String> call(List<String> s) throws Exception {
                i++;
                return new Tuple2<String, String>(i+"",s.get(num));
            }
        });
        System.out.println("rdd: "+x.collectAsMap());
        //0:x 1:b  2:a
        rddMap.put(key,x);
    }



}
