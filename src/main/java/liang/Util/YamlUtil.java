package liang.Util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import liang.bean.Feature;

import java.io.FileNotFoundException;
import java.io.FileReader;
public class YamlUtil {

    public Feature readYaml(String fileName) throws FileNotFoundException, YamlException {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource(fileName).getPath();
        YamlReader reader = new YamlReader(new FileReader(path));
        Feature feature = reader.read(Feature.class);
        return feature;
    }

    public static void main(String[] args) {
        String a = "people.age";
        String[] b =  a.split("\\.");
        for (String x : b){
            System.out.println(x);
        }
        String kk = " {\"people\":{\"age\":1,\"name\":\"amy\"}}";
        JSONObject jsonObject = JSON.parseObject(kk);
        String paramValue = "";
        for (int i=0;i<b.length;i++){
            if (i == b.length-1){
                paramValue = jsonObject.getString(b[i]);
                return;
            }
            jsonObject = jsonObject.getJSONObject(b[i]);
        }

    }
}
