package liang.service;

import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.lianjia.aisearch.featurefu.expr.Expr;
import com.lianjia.aisearch.featurefu.expr.Expression;
import com.lianjia.aisearch.featurefu.expr.VariableRegistry;
import com.lianjia.aisearch.machinelearning.operator.Bean.*;
import com.lianjia.aisearch.machinelearning.operator.Service.Impl.PretreatmentServiceImpl;
import com.lianjia.aisearch.machinelearning.operator.Service.PretreatmentService;
import javafx.scene.control.Separator;
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
import java.io.File;
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
        File dumpFile = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\entirety.yaml");
        YamlUtil entiretyHandler = new YamlUtil();
        Feature feature = entiretyHandler.readYaml();



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
        List<Source> sources = feature.getSources();
        Map<String, JavaPairRDD> rddMap = new HashMap<String, JavaPairRDD>();
        for (Source source : sources) {
            getSource(javaSparkContext, source, rddMap);
        }

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



    private static void getSource(JavaSparkContext javaSparkContext, Source source,
                                  Map<String, JavaPairRDD> rddMap) {

        List<VariableObj> source_1_variables =  source.getVariables();
        for (VariableObj variable : source_1_variables) {
            //key: x b a
            System.out.println(" variable name : "+variable.getName());
            getPariRDD(variable.getName(), variable, javaSparkContext, source, rddMap);
        }

    }


    private static void getPariRDD(String key, final VariableObj variable,
                                   JavaSparkContext javaSparkContext, Source source,
                                   Map<String, JavaPairRDD> rddMap) {
        String path = source.getPath();
        final String columnSplitSymbol = source.getColumnSplitSymbol();
        final int num = variable.getNum();
        final int joinLineNum = variable.getJoinLineNum();
        final Filter filter = variable.getFilter();
        final Missing missing = variable.getMissing();
        final String dataType = variable.getDataType();
        final String param = variable.getParam();

        JavaRDD<String> data = javaSparkContext.textFile(path);
        JavaRDD<String> lines = data.flatMap(new FlatMapFunction<String, String>() {
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\r\\n")).iterator();
            }
        });
        PretreatmentService pretreatmentService = new PretreatmentServiceImpl();
        JavaRDD<List<String>> datas = pretreatmentService.separate(lines,columnSplitSymbol); ;
        //分解
        if ("json".equals(dataType)){
            datas = datas.map(new Function<List<String>, List<String>>() {
                public List<String> call(List<String> s) throws Exception {
                    String kk = s.get(num);
                    JSONObject jsonObject = JSONObject.parseObject(kk);
                    String[] fields = param.split("\\.");
                    String paramValue = "";
                    for (int i=0;i<fields.length;i++){
                        if (i == fields.length-1){
                            paramValue = jsonObject.getString(fields[i]);
                        }else {
                            jsonObject = jsonObject.getJSONObject(fields[i]);
                        }
                    }
                    s.set(num,paramValue);
                    return s;
                }
            });
        }
        System.out.println("datas:"+datas.collect());

        //filter
        JavaRDD<List<String>> filterRDD1 ;

        if ("json".equals(dataType)){
            filterRDD1 = datas.filter(new Function<List<String>, Boolean>() {
                public Boolean call(List<String> s) throws Exception {
                    String kk = s.get(num);
                    if (null != filter){
                        String filterSymbol = filter.getSymbol();
                        Double value = filter.getValue();
                        StringBuilder stringBuilder = new StringBuilder("");
                        stringBuilder.append("(");
                        stringBuilder.append(filterSymbol);
                        stringBuilder.append(" ");
                        stringBuilder.append(kk);
                        stringBuilder.append(" ");
                        stringBuilder.append(value);
                        stringBuilder.append(")");
                        return Expression.evaluate(stringBuilder.toString()) == 0D;
                    }else {
                        return true;
                    }
                }
            });
        }else {
            JavaRDD<List<String>> s = pretreatmentService.separate(lines,columnSplitSymbol);
            filterRDD1 = pretreatmentService.filter(s,variable);
        }

        System.out.println("filterRDD1:"+filterRDD1.collect());

        JavaRDD<List<String>> missingRDD ;

        if ("json".equals(dataType)){
            missingRDD = filterRDD1.map(new Function<List<String>, List<String>>() {
                public List<String> call(List<String> s) throws Exception {
                    String kk = s.get(num);
                    if (null != missing) {
                        String missingWay = missing.getWay();
                        Double value = missing.getValue();
                        if (StringUtils.isBlank(kk)) {
                            if ("assign".equals(missingWay)) {
                                s.set(num,value+"");
                            }
                        }
                    }
                    return s;
                }
            });
        }else {
            missingRDD = pretreatmentService.missingFill(filterRDD1,variable);
        }
        System.out.println("missingRDD:"+missingRDD.collect());

        JavaPairRDD<String, String> x = missingRDD.mapToPair(new PairFunction<List<String>, String, String>() {
            public Tuple2<String, String> call(List<String> s) throws Exception {
                return new Tuple2<String, String>(s.get(joinLineNum), s.get(num));
            }
        });
        System.out.println("rdd: " + x.collectAsMap());
        rddMap.put(key, x);
    }


}
