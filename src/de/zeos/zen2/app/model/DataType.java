package de.zeos.zen2.app.model;

public class DataType {
    private DataClass dataClass;
    private ScalarDataType type;
    private Enumeration enumeration;
    private Entity refEntity;
    private boolean lazy;
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

    public Enumeration getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(Enumeration enumeration) {
        this.enumeration = enumeration;
    }

    public Entity getRefEntity() {
        return refEntity;
    }

    public void setRefEntity(Entity refEntity) {
        this.refEntity = refEntity;
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
