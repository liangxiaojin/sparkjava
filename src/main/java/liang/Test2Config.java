package liang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Test2Config {

    public static void main(String[] args) {
        Set<String>  a = new HashSet<String>();
        a.add("1");
        a.add("2");
        List<String> bb  =  new ArrayList<String>(a);
        while (a.iterator().hasNext()){
            System.out.println(a.iterator().next());
            a.iterator().next();
        }
    }
}
