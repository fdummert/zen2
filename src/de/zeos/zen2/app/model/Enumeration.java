package de.zeos.zen2.app.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "zen2.enumeration")
public class Enumeration {
    private String id;
    private List<String> constants;
    @org.springframework.data.mongodb.core.mapping.Field("_system")
    private boolean system;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getConstants() {
        return constants;
    }

    public void setConstants(List<String> constants) {
        this.constants = constants;
    }

    public boolean isSystem() {
        return this.system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }
}
