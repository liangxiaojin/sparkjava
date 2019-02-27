package liang.bean;

import java.util.List;

public class HdfsSource {
    private String path;
    private String columnSplitSymbol;
    private Field primaryField;
    private List<Field> originFields;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getColumnSplitSymbol() {
        return columnSplitSymbol;
    }

    public void setColumnSplitSymbol(String columnSplitSymbol) {
        this.columnSplitSymbol = columnSplitSymbol;
    }

    public Field getPrimaryField() {
        return primaryField;
    }

    public void setPrimaryField(Field primaryField) {
        this.primaryField = primaryField;
    }

    public List<Field> getOriginFields() {
        return originFields;
    }

    public void setOriginFields(List<Field> originFields) {
        this.originFields = originFields;
    }
}
