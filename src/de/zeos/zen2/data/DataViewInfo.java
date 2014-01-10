package de.zeos.zen2.data;

import java.util.ArrayList;
import java.util.List;

import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.FieldView;

public class DataViewInfo {
    private DataView dataView;
    private EntityInfo entity;

    public DataViewInfo(DataView dataView) {
        this.dataView = dataView;
        this.entity = new EntityInfo(dataView.getEntity(), dataView.getFields() == null ? new ArrayList<FieldView>() : new ArrayList<FieldView>(dataView.getFields()));
    }

    public String getId() {
        return this.dataView.getId();
    }

    public String getScope() {
        return this.dataView.getScope();
    }

    public boolean isPushable() {
        return this.dataView.isPushable();
    }

    public List<String> getPushScopes() {
        return this.dataView.getPushScopes();
    }

    public EntityInfo getEntity() {
        return this.entity;
    }
}