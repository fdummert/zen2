package de.zeos.zen2.app.model;

import java.util.List;

public class Index {
    private String id;
    private List<Field> fields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

}
