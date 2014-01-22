package de.zeos.zen2.data;

import java.util.List;

import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.app.model.Field;
import de.zeos.zen2.app.model.FieldView;
import de.zeos.zen2.app.model.PkType;
import de.zeos.zen2.db.InternalDBAccessor;

public class FieldInfo {
    private Field field;
    private FieldView fieldView;
    private DataTypeInfo dataTypeInfo;

    public FieldInfo(ModelInfo modelInfo, DataViewInfo dataViewInfo, InternalDBAccessor accessor, Field field, FieldView fieldView, String prefix, List<FieldView> fieldViews) {
        this.field = field;
        this.fieldView = fieldView;
        this.dataTypeInfo = new DataTypeInfo(modelInfo, dataViewInfo, accessor, field.getType(), fieldView, prefix, field.getName(), fieldViews);
    }

    public String getName() {
        return field.getName();
    }

    public boolean isMandatory() {
        if (fieldView != null && fieldView.getMandatory() != null)
            return fieldView.getMandatory();
        return field.isMandatory();
    }

    public boolean isReadOnly() {
        if (fieldView != null && fieldView.getReadOnly() != null)
            return fieldView.getReadOnly();
        return field.isReadOnly();
    }

    public boolean isPk() {
        return field.isPk();
    }

    public PkType getPkType() {
        return field.getPkType();
    }

    public DataTypeInfo getType() {
        return this.dataTypeInfo;
    }

    public boolean isComplex() {
        return this.dataTypeInfo.getDataClass() == DataClass.ENTITY || (this.dataTypeInfo.getDataClass() == DataClass.LIST && this.dataTypeInfo.getRefEntityId() != null);
    }
}
