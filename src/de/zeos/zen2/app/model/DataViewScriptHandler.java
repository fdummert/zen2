package de.zeos.zen2.app.model;

public class DataViewScriptHandler extends ScriptHandler {

    public enum TriggerPoint {
        BEFORE_PROCESSING, BEFORE, AFTER
    }

    public enum TriggerMode {
        ALL, CREATE, READ, UPDATE, DELETE
    }

    private TriggerPoint triggerPoint;
    private TriggerMode triggerMode;

    public TriggerPoint getTriggerPoint() {
        return this.triggerPoint;
    }

    public void setTriggerPoint(TriggerPoint triggerPoint) {
        this.triggerPoint = triggerPoint;
    }

    public TriggerMode getTriggerMode() {
        return this.triggerMode;
    }

    public void setTriggerMode(TriggerMode triggerMode) {
        this.triggerMode = triggerMode;
    }
}
