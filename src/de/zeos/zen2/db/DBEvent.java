package de.zeos.zen2.db;

import java.util.EventObject;
import java.util.Map;

import de.zeos.zen2.app.model.DataView.CommandMode;

public class DBEvent extends EventObject {
    private static final long serialVersionUID = -2761057561740666284L;

    public enum Type {
        BEFORE_PROCESSING, BEFORE, AFTER
    }

    private String app;
    private CommandMode mode;
    private Type type;
    private Map<String, Object> query;
    private Object result;

    public DBEvent(String app, Object source, CommandMode mode, Type type, Map<String, Object> query, Object result) {
        super(source);
        this.app = app;
        this.mode = mode;
        this.type = type;
        this.query = query;
        this.result = result;
    }

    public String getApp() {
        return app;
    }

    public CommandMode getMode() {
        return mode;
    }

    public Type getType() {
        return type;
    }

    public Map<String, Object> getQuery() {
        return query;
    }

    public Object getResult() {
        return result;
    }
}
