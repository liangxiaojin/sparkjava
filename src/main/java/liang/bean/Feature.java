package liang.bean;

import java.util.List;

public class Feature {
    private String formula;
    private List<HdfsSource> hdfsSource;
    private List<MysqlSource> mysqlSource;


    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public List<HdfsSource> getHdfsSource() {
        return hdfsSource;
    }

    public void setHdfsSource(List<HdfsSource> hdfsSource) {
        this.hdfsSource = hdfsSource;
    }
}
