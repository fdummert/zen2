package de.zeos.zen2.app.model;

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
    private boolean system;

    private ScriptHandler beforeHandler;
    private ScriptHandler beforeProcessingCreateHandler;
    private ScriptHandler beforeCreateHandler;
    private ScriptHandler afterCreateHandler;
    private ScriptHandler beforeProcessingReadHandler;
    private ScriptHandler beforeReadHandler;
    private ScriptHandler afterReadHandler;
    private ScriptHandler beforeProcessingUpdateHandler;
    private ScriptHandler beforeUpdateHandler;
    private ScriptHandler afterUpdateHandler;
    private ScriptHandler beforeProcessingDeleteHandler;
    private ScriptHandler beforeDeleteHandler;
    private ScriptHandler afterDeleteHandler;
    private ScriptHandler afterHandler;

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

    public boolean isSystem() {
        return this.system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public ScriptHandler getBeforeHandler() {
        return this.beforeHandler;
    }

    public void setBeforeHandler(ScriptHandler beforeHandler) {
        this.beforeHandler = beforeHandler;
    }

    public ScriptHandler getBeforeProcessingCreateHandler() {
        return this.beforeProcessingCreateHandler;
    }

    public void setBeforeProcessingCreateHandler(ScriptHandler beforeProcessingCreateHandler) {
        this.beforeProcessingCreateHandler = beforeProcessingCreateHandler;
    }

    public ScriptHandler getBeforeCreateHandler() {
        return this.beforeCreateHandler;
    }

    public void setBeforeCreateHandler(ScriptHandler beforeCreateHandler) {
        this.beforeCreateHandler = beforeCreateHandler;
    }

    public ScriptHandler getAfterCreateHandler() {
        return this.afterCreateHandler;
    }

    public void setAfterCreateHandler(ScriptHandler afterCreateHandler) {
        this.afterCreateHandler = afterCreateHandler;
    }

    public ScriptHandler getBeforeProcessingReadHandler() {
        return this.beforeProcessingReadHandler;
    }

    public void setBeforeProcessingReadHandler(ScriptHandler beforeProcessingReadHandler) {
        this.beforeProcessingReadHandler = beforeProcessingReadHandler;
    }

    public ScriptHandler getBeforeReadHandler() {
        return this.beforeReadHandler;
    }

    public void setBeforeReadHandler(ScriptHandler beforeReadHandler) {
        this.beforeReadHandler = beforeReadHandler;
    }

    public ScriptHandler getAfterReadHandler() {
        return this.afterReadHandler;
    }

    public void setAfterReadHandler(ScriptHandler afterReadHandler) {
        this.afterReadHandler = afterReadHandler;
    }

    public ScriptHandler getBeforeProcessingUpdateHandler() {
        return this.beforeProcessingUpdateHandler;
    }

    public void setBeforeProcessingUpdateHandler(ScriptHandler beforeProcessingUpdateHandler) {
        this.beforeProcessingUpdateHandler = beforeProcessingUpdateHandler;
    }

    public ScriptHandler getBeforeUpdateHandler() {
        return this.beforeUpdateHandler;
    }

    public void setBeforeUpdateHandler(ScriptHandler beforeUpdateHandler) {
        this.beforeUpdateHandler = beforeUpdateHandler;
    }

    public ScriptHandler getAfterUpdateHandler() {
        return this.afterUpdateHandler;
    }

    public void setAfterUpdateHandler(ScriptHandler afterUpdateHandler) {
        this.afterUpdateHandler = afterUpdateHandler;
    }

    public ScriptHandler getBeforeProcessingDeleteHandler() {
        return this.beforeProcessingDeleteHandler;
    }

    public void setBeforeProcessingDeleteHandler(ScriptHandler beforeProcessingDeleteHandler) {
        this.beforeProcessingDeleteHandler = beforeProcessingDeleteHandler;
    }

    public ScriptHandler getBeforeDeleteHandler() {
        return this.beforeDeleteHandler;
    }

    public void setBeforeDeleteHandler(ScriptHandler beforeDeleteHandler) {
        this.beforeDeleteHandler = beforeDeleteHandler;
    }

    public ScriptHandler getAfterDeleteHandler() {
        return this.afterDeleteHandler;
    }

    public void setAfterDeleteHandler(ScriptHandler afterDeleteHandler) {
        this.afterDeleteHandler = afterDeleteHandler;
    }

    public ScriptHandler getAfterHandler() {
        return this.afterHandler;
    }

    public void setAfterHandler(ScriptHandler afterHandler) {
        this.afterHandler = afterHandler;
    }
}
