package de.zeos.zen2.app.model;

import java.util.List;

public class Enumeration {
    private String id;
    private List<String> constants;

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
}
