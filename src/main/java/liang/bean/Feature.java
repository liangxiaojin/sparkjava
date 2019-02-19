package liang.bean;

import java.util.HashMap;

public class Feature {
    private String source;
    private String formula;
    private HashMap<String,Integer> variables;
    private String path;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public HashMap<String, Integer> getVariables() {
        return variables;
    }

    public void setVariables(HashMap<String, Integer> variables) {
        this.variables = variables;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
