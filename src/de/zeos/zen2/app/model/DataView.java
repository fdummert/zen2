package de.zeos.zen2.app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DataView {

    public enum CommandMode {
        CREATE, READ, UPDATE, DELETE
    }

    private String id;
    private Entity entity;
    private List<FieldView> fields;

    private String scope;
    private List<String> pushScopes;

    private Set<CommandMode> allowedModes;
    private boolean pushable;

    private String dataViewHandler;
    private boolean dataViewHandlerValid;
    private List<ScriptHandlerError> dataViewHandlerErrors = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public List<FieldView> getFields() {
        return fields;
    }

    public void setFields(List<FieldView> fields) {
        this.fields = fields;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getPushScopes() {
        return pushScopes;
    }

    public void setPushScopes(List<String> pushScopes) {
        this.pushScopes = pushScopes;
    }

    public Set<CommandMode> getAllowedModes() {
        return allowedModes;
    }

    public void setAllowedModes(Set<CommandMode> allowedModes) {
        this.allowedModes = allowedModes;
    }

    public boolean isPushable() {
        return pushable;
    }

    public void setPushable(boolean pushable) {
        this.pushable = pushable;
    }

    public String getDataViewHandler() {
        return dataViewHandler;
    }

    public void setDataViewHandler(String dataViewHandler) {
        this.dataViewHandler = dataViewHandler;
    }

    public boolean isDataViewHandlerValid() {
        return dataViewHandlerValid;
    }

    public void setDataViewHandlerValid(boolean dataViewHandlerValid) {
        this.dataViewHandlerValid = dataViewHandlerValid;
    }

    public List<ScriptHandlerError> getDataViewHandlerErrors() {
        return dataViewHandlerErrors;
    }

    public void setDataViewHandlerErrors(List<ScriptHandlerError> dataViewHandlerErrors) {
        this.dataViewHandlerErrors = dataViewHandlerErrors;
    }
}
