package de.zeos.zen2.app.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "zen2.entity")
public class Entity {
    private String id;
    private String parentEntityId;
    private List<Field> fields;
    private List<Index> indexes;
    private boolean embeddable;
    @org.springframework.data.mongodb.core.mapping.Field("_system")
    private boolean system;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentEntityId() {
        return this.parentEntityId;
    }

    public void setParentEntityId(String parentEntityId) {
        this.parentEntityId = parentEntityId;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<Index> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<Index> indexes) {
        this.indexes = indexes;
    }

    public boolean isEmbeddable() {
        return embeddable;
    }

    public void setEmbeddable(boolean embeddable) {
        this.embeddable = embeddable;
    }

    public boolean isSystem() {
        return this.system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }
}
