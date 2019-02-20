package liang.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Feature {
    private String formula;
    private String variables;
    private List<Map<String,Object>> sources;

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public List<Map<String, Object>> getSources() {
        return sources;
    }

    public void setSources(List<Map<String, Object>> sources) {
        this.sources = sources;
    }
}
