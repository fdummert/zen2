package de.zeos.zen2.db.mongo;

import java.util.Map;

public class Ref {
    private String property;
    private Object id;
    private Map<String, Object> refObj;
    private String entity;
    private boolean lazy;

    public Ref(String property, String entity) {
        this.property = property;
        this.entity = entity;
    }

    public String getProperty() {
        return property;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Map<String, Object> getRefObj() {
        return refObj;
    }

    public void setRefObj(Map<String, Object> refObj) {
        this.refObj = refObj;
    }

    public String getEntity() {
        return entity;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }
}