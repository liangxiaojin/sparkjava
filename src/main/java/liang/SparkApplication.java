//package liang;
//
//import com.lianjia.search.bean.Action;
//import com.lianjia.search.service.StoreDataHandler;
//import com.lianjia.search.service.SugQueryHandler;
//import com.lianjia.search.util.DataConstants;
//import com.lianjia.search.util.DataContext;
//import com.lianjia.search.util.DateUtil;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.FileSystemXmlApplicationContext;
//
//import java.io.File;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * @author xupengcheng
// * @since 2019/1/11
// */
//public class SparkApplication {
//
//
//    private static String dataPath = "/home/work/xupengcheng/schedule_task/data/sug_click/";
//
//    public SparkApplication() {
//        init();
//    }
//
//    private static void init() {
//        String path = System.getProperty("user.dir");
//        File file = new File(path);
//        String abPath = "file:" + file.getParent() + "/config/applicationContext.xml";
//        System.out.println("get config from:" + abPath);
//        ApplicationContext applicationContext = new FileSystemXmlApplicationContext
//                (abPath);
//        DataContext.setApplicationContext(applicationContext);
//    }
//
//    public static void main(String[] args) {
//
//    }
//
//}
