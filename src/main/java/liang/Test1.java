package liang;

import com.linkedin.featurefu.expr.Expr;
import com.linkedin.featurefu.expr.Expression;
import com.linkedin.featurefu.expr.VariableRegistry;
import liang.bean.Features;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
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
        File dumpFile=new File(System.getProperty("user.dir") + "\\src\\main\\resources\\featureConfig.yaml");
        Features features = null;
        try {
            features = Yaml.loadType(dumpFile, Features.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<Double> results = new ArrayList<Double>();
        for (Object o : features.getFeatures()){
            Map<String,Object> feature = (Map) o;
            //sparkContext.setLogLevel("INFO");
            JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);
            // Create the DataFrame
            JavaRDD<String> data = javaSparkContext.textFile(feature.get("path").toString());
            JavaRDD<List<String[]>> ss = data.map(new Function<String, List<String[]>>() {
                public List<String[]> call(String s) throws Exception {
                    List<String[]> values = new ArrayList<String[]>();
                    String[] lines = s.split("\\r\\n");
                    for (String a : lines) {
                        String[] columns = a.split(",");
                        values.add(columns);
                    }
                    return values;
                }
            });
            System.out.println(ss.collect());
            List<List<String[]>> lines = ss.collect();
            VariableRegistry variableRegistry = new VariableRegistry();
            //parse expression with variables, use variableRegistry to register variables
            Expr expression = Expression.parse(feature.get("formula").toString(), variableRegistry);
            //retrieve variables from variableRegistry by name
            Map<String,Integer> variables = (Map<String, Integer>) feature.get("variables");
            List<String> variableKeyList = new ArrayList<String>(variables.keySet()) ;
            //set variable values
            for (List<String[]> line : lines) {
                for (String[] line1 : line){
                    Long start = System.currentTimeMillis();
                    for (String variable : variableKeyList){
                        variableRegistry.findVariable(variable).setValue(Integer.valueOf(line1[variables.get(variable)]));
                    }
                    results.add(expression.evaluate());
                    Long end = System.currentTimeMillis();
                    System.out.println("cost:"+ (end - start));
                }

            }
        }
        System.out.println(results);
        //results:[0.9933071490757153, 0.999999999994891]
    }
}
