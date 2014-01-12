package de.zeos.zen2.data;

import java.util.HashMap;
import java.util.Map;

import de.zeos.zen2.app.model.Enumeration;

public class ModelInfo {
    private Map<String, DataViewInfo> dataViews = new HashMap<>();
    private Map<String, EntityInfo> entities = new HashMap<>();
    private Map<String, Enumeration> enumerations = new HashMap<>();

    public Map<String, DataViewInfo> getDataViews() {
        return dataViews;
    }

    void addDataView(DataViewInfo dataViewInfo) {
        if (!dataViews.containsKey(dataViewInfo.getId()))
            dataViews.put(dataViewInfo.getId(), dataViewInfo);
    }

    public Map<String, Enumeration> getEnumerations() {
        return enumerations;
    }

    void addEnumeration(Enumeration enumeration) {
        if (!enumerations.containsKey(enumeration.getId()))
            enumerations.put(enumeration.getId(), enumeration);
    }

    public Map<String, EntityInfo> getEntities() {
        return entities;
    }

    EntityInfo getEntity(String dataViewId, String entityId) {
        return entities.get(dataViewId + ":" + entityId);
    }

    void addEntity(DataViewInfo dataViewInfo, EntityInfo entityInfo) {
        String path = dataViewInfo.getId() + ":" + entityInfo.getId();
        if (!entities.containsKey(path))
            entities.put(path, entityInfo);
    }
}
