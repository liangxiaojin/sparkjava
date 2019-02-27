package liang.service;

import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.yamlbeans.YamlException;
import com.lianjia.aisearch.featurefu.expr.Expression;
import com.lianjia.aisearch.machinelearning.operator.Bean.Filter;
import com.lianjia.aisearch.machinelearning.operator.Bean.Missing;
import com.lianjia.aisearch.machinelearning.operator.Service.Impl.PretreatmentServiceImpl;
import com.lianjia.aisearch.machinelearning.operator.Service.PretreatmentService;
import liang.Util.YamlUtil;
import liang.bean.*;
import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import java.io.FileNotFoundException;
import java.util.*;

public class EntiretyHandler  {

    static transient SparkContext sparkContext;
    private final static transient SparkConf SPARK_CONF = new SparkConf();

    public static void main(String[] args) throws FileNotFoundException, YamlException {
        SPARK_CONF.setMaster("local");
        SPARK_CONF.setAppName("liang");
        SPARK_CONF.set("spark.hadoop.validateOutputSpecs", "false");
        SPARK_CONF.set("spark.akka.frameSize", "300");
        SPARK_CONF.set("spark.driver.maxResultSize", "6g");
        SPARK_CONF.setAppName("SugLog");
        sparkContext = new SparkContext(SPARK_CONF);
        //读取yaml文件
        YamlUtil entiretyHandler = new YamlUtil();
        Feature feature = entiretyHandler.readYaml("newentirety.yaml");


        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);
        //获取yaml中的公式，公式中的变量
        final String formula = feature.getFormula();
        Map<Integer, String> variableMap = new HashMap<Integer, String>();
        List<String> variables = new ArrayList<String>();
        Expression.variables(formula, variables);

        //公式变量index及变量的map
        for (int i = 0; i < variables.size(); i++) {
            variableMap.put(i, variables.get(i));
        }

        final List<Integer> paramOrder = new ArrayList<Integer>(variableMap.keySet());

        //通过数据源获取需要的数据，放入rddMap，key是变量名，value是具体列值
        Map<String, JavaPairRDD> rddMap = new HashMap<String, JavaPairRDD>();
        getSource(javaSparkContext, feature, rddMap);


        //按变量顺序join rdd
        JavaPairRDD  allRdd = rddMap.get(variableMap.get(paramOrder.get(0)));
        for (int i = 1; i < paramOrder.size(); i++) {
            System.out.println(variableMap.get(paramOrder.get(i)));
            allRdd = allRdd.join(rddMap.get(variableMap.get(paramOrder.get(i))));
        }

        System.out.println("allRdd:" + allRdd.collectAsMap());

        //放入公式计算
        JavaPairRDD<String, Double> ll = allRdd.mapValues(new Function<Tuple2<String, String>, Double>() {
            public Double call(Tuple2<String, String> tuple2) {
                String tuple = tuple2.toString();
                String[] columns = tuple.replace("(", "").replace(")", "").split(",");
                return Expression.evaluate(formula, Arrays.asList(columns));
            }
        });
        System.out.println(ll.collectAsMap());


    }



    private static void getSource(JavaSparkContext javaSparkContext,Feature  feature,
                                  Map<String, JavaPairRDD> rddMap) {
        List<HdfsSource> hdfsSources = feature.getHdfsSource();
        for (HdfsSource hdfsSource : hdfsSources){
            String splitSymbol = hdfsSource.getColumnSplitSymbol();
            List<Field> fields = hdfsSource.getOriginFields();
            String path = hdfsSource.getPath();
            Field primaryField = hdfsSource.getPrimaryField();
            for (Field field : fields) {
                //key: x b a
                System.out.println(" variable name : "+field.getVariableName());
                getPariRDD(path, splitSymbol,field, javaSparkContext, rddMap,primaryField.getNum());
            }
        }
    }


    private static void getPariRDD(String path,final String splitSymbol, final Field field,
                                   JavaSparkContext javaSparkContext, Map<String, JavaPairRDD> rddMap,
                                   final int primaryNum) {
        String key = field.getVariableName();
        final int num = field.getNum();
        final Filter filter = field.getFilter();
        final Missing missing = field.getMissing();
        final String dataType = field.getDataType();
        final String fieldName = field.getFieldName();

        JavaRDD<String> data = javaSparkContext.textFile(path);
        JavaRDD<String> lines = data.flatMap(new FlatMapFunction<String, String>() {
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\r\\n")).iterator();
            }
        });
        PretreatmentService pretreatmentService = new PretreatmentServiceImpl();
        JavaRDD<List<String>> datas = pretreatmentService.separate(lines,splitSymbol,num,fieldName,dataType);
        System.out.println("datas:"+datas.collect());

        //filter
        JavaRDD<List<String>> filterRDD1 = pretreatmentService.filter(datas,num,filter);
        System.out.println("filterRDD1:"+filterRDD1.collect());

        JavaRDD<List<String>> missingRDD = pretreatmentService.missingFill(filterRDD1,num,missing);
        System.out.println("missingRDD:"+missingRDD.collect());

        JavaPairRDD<String, String> x = missingRDD.mapToPair(new PairFunction<List<String>, String, String>() {
            public Tuple2<String, String> call(List<String> s) throws Exception {
                return new Tuple2<String, String>(s.get(primaryNum), s.get(num));
            }
        });
        System.out.println("rdd: " + x.collectAsMap());
        rddMap.put(key, x);
    }


}
