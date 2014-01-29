package de.zeos.zen2.app.model;

import java.util.List;

public class DataViewScriptHandler extends ScriptHandler {

    public enum TriggerPoint {
        BEFORE_PROCESSING, BEFORE, AFTER
    }

    public enum TriggerMode {
        ALL, CREATE, READ, UPDATE, DELETE
    }

    private TriggerPoint triggerPoint;
    private List<TriggerMode> triggerModes;

    public TriggerPoint getTriggerPoint() {
        return this.triggerPoint;
    }

    public void setTriggerPoint(TriggerPoint triggerPoint) {
        this.triggerPoint = triggerPoint;
    }

    public List<TriggerMode> getTriggerModes() {
        return this.triggerModes;
    }

    public void setTriggerModes(List<TriggerMode> triggerModes) {
        this.triggerModes = triggerModes;
    }
}
