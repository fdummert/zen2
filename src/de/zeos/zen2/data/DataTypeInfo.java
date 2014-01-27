package de.zeos.zen2.data;

import java.util.List;

import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.app.model.DataType;
import de.zeos.zen2.app.model.FieldView;
import de.zeos.zen2.app.model.ScalarDataType;
import de.zeos.zen2.db.InternalDBAccessor;

public class DataTypeInfo {
    private ModelInfo modelInfo;
    private DataViewInfo dataViewInfo;

    private DataType dataType;
    private FieldView fieldView;
    private String refEntityId;
    private String enumerationId;

    public DataTypeInfo(ModelInfo modelInfo, DataViewInfo dataViewInfo, InternalDBAccessor accessor, DataType dataType, FieldView fieldView, String prefix, String fieldName, List<FieldView> fieldViews) {
        this.modelInfo = modelInfo;
        this.dataViewInfo = dataViewInfo;
        this.dataType = dataType;
        this.fieldView = fieldView;
        this.enumerationId = dataType.getEnumerationId();
        if (enumerationId != null && modelInfo.getEnumerations().get(enumerationId) == null)
            modelInfo.addEnumeration(accessor.getEnumeration(enumerationId));
        this.refEntityId = dataType.getRefEntityId();
        if (refEntityId != null) {
            if (modelInfo.getEntity(dataViewInfo.getId(), refEntityId) == null) {
                modelInfo.addEntity(dataViewInfo, new EntityInfo(modelInfo, dataViewInfo, accessor, accessor.getEntity(refEntityId), prefix.length() == 0 ? fieldName : prefix + "." + fieldName, fieldViews));
            }
        }
    }

    public DataClass getDataClass() {
        return this.dataType.getDataClass();
    }

    public ScalarDataType getType() {
        return this.dataType.getType();
    }

    public String getEnumerationId() {
        return enumerationId;
    }

    public String getRefEntityId() {
        return refEntityId;
    }

    public String getDataViewId() {
        if (this.fieldView != null && this.fieldView.getDataViewId() != null)
            return this.fieldView.getDataViewId();
        return this.dataType.getDataViewId();
    }

    public EntityInfo resolveRefEntity() {
        return this.modelInfo.getEntity(this.dataViewInfo.getId(), refEntityId);
    }

    public boolean isLazy() {
        if (this.fieldView != null && this.fieldView.getLazy() != null)
            return this.fieldView.getLazy();
        return this.dataType.isLazy();
    }

    public boolean isCascade() {
        if (this.fieldView != null && this.fieldView.getCascade() != null)
            return this.fieldView.getCascade();
        return this.dataType.isCascade();
    }

    public String getBackRef() {
        return this.dataType.getBackRef();
    }

    public boolean isInverse() {
        return this.dataType.isInverse();
    }
}
