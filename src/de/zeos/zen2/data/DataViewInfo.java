package de.zeos.zen2.data;

import java.util.ArrayList;
import java.util.List;

import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.FieldView;
import de.zeos.zen2.app.model.DataView.OverwriteMode;
import de.zeos.zen2.db.InternalDBAccessor;

public class DataViewInfo {
    private DataView dataView;
    private EntityInfo entity;

    public DataViewInfo(ModelInfo modelInfo, InternalDBAccessor accessor, DataView dataView) {
        this.dataView = dataView;
        modelInfo.addDataView(this);
        this.entity = new EntityInfo(modelInfo, this, accessor, dataView.getEntity(), dataView.getFields() == null ? new ArrayList<FieldView>() : new ArrayList<FieldView>(dataView.getFields()));
    }

    public String getId() {
        return this.dataView.getId();
    }

    public OverwriteMode getOverwriteMode() {
        return this.dataView.getOverwriteMode();
    }

    public boolean isSystem() {
        return this.dataView.isSystem();
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