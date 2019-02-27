package liang.bean;

import com.lianjia.aisearch.machinelearning.operator.Bean.Filter;
import com.lianjia.aisearch.machinelearning.operator.Bean.Missing;

public class Field {

    public static final String DATATYPE_JSON="json";
    public static final String DATATYPE_TEXT="text";

    private String variableName;
    private Integer num;
    private String dataType;
    private String fieldName;
    private Missing missing;
    private Filter filter;


    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Missing getMissing() {
        return missing;
    }

    public void setMissing(Missing missing) {
        this.missing = missing;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
