package de.zeos.zen2.data;

import java.util.List;

import de.zeos.zen2.app.model.Field;
import de.zeos.zen2.app.model.FieldView;

public class FieldInfo {
    private Field field;
    private FieldView fieldView;
    private DataTypeInfo dataTypeInfo;

    public FieldInfo(Field field, FieldView fieldView, String prefix, List<FieldView> fieldViews) {
        this.field = field;
        this.fieldView = fieldView;
        this.dataTypeInfo = new DataTypeInfo(field.getType(), fieldView, prefix, field.getName(), fieldViews);
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

    public DataTypeInfo getType() {
        return this.dataTypeInfo;
    }
}
