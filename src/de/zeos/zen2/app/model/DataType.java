package de.zeos.zen2.app.model;

public class DataType {
    private DataClass dataClass;
    private ScalarDataType type;
    private String enumerationId;
    private String refEntityId;
    private String dataViewId;
    private boolean lazy = true;
    private boolean cascade;

    public DataClass getDataClass() {
        return dataClass;
    }

    public void setDataClass(DataClass dataClass) {
        this.dataClass = dataClass;
    }

    public ScalarDataType getType() {
        return type;
    }

    public void setType(ScalarDataType type) {
        this.type = type;
    }

    public String getEnumerationId() {
        return enumerationId;
    }

    public void setEnumerationId(String enumerationId) {
        this.enumerationId = enumerationId;
    }

    public String getRefEntityId() {
        return refEntityId;
    }

    public void setRefEntityId(String refEntityId) {
        this.refEntityId = refEntityId;
    }

    public String getDataViewId() {
        return dataViewId;
    }

    public void setDataViewId(String dataViewId) {
        this.dataViewId = dataViewId;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public boolean isCascade() {
        return cascade;
    }

    public void setCascade(boolean cascade) {
        this.cascade = cascade;
    }
}
