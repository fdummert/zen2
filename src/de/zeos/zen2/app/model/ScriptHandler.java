package de.zeos.zen2.app.model;

import java.util.ArrayList;
import java.util.List;

public class ScriptHandler {
    private String id;
    private String source;
    private boolean valid;
    private List<ScriptHandlerError> errors = new ArrayList<>();
    private List<ScriptHandlerConsoleEntry> consoleEntries = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<ScriptHandlerError> getErrors() {
        return errors;
    }

    public void setErrors(List<ScriptHandlerError> errors) {
        this.errors = errors;
    }

    public List<ScriptHandlerConsoleEntry> getConsoleEntries() {
        return consoleEntries;
    }

    public void setConsoleEntries(List<ScriptHandlerConsoleEntry> consoleEntries) {
        this.consoleEntries = consoleEntries;
    }
}
