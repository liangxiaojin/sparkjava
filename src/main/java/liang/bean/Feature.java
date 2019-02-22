package liang.bean;

import java.util.List;
import java.util.Map;

public class Feature {
    private String formula;
    private List<Map<String,Object>> sources;
    private Integer numberOfOperands;
    private String symbol;
    private List<Map<String,Object>> params;

    public Integer getNumberOfOperands() {
        return numberOfOperands;
    }

    public void setNumberOfOperands(Integer numberOfOperands) {
        this.numberOfOperands = numberOfOperands;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<Map<String, Object>> getParams() {
        return params;
    }

    public void setParams(List<Map<String, Object>> params) {
        this.params = params;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }


    public List<Map<String, Object>> getSources() {
        return sources;
    }

    public void setSources(List<Map<String, Object>> sources) {
        this.sources = sources;
    }
}
