package de.zeos.zen2.data;

import java.util.List;

import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.app.model.DataType;
import de.zeos.zen2.app.model.Enumeration;
import de.zeos.zen2.app.model.FieldView;
import de.zeos.zen2.app.model.ScalarDataType;

public class DataTypeInfo {
    private DataType dataType;
    private FieldView fieldView;
    private EntityInfo entityInfo;

    public DataTypeInfo(DataType dataType, FieldView fieldView, String prefix, String fieldName, List<FieldView> fieldViews) {
        this.dataType = dataType;
        this.fieldView = fieldView;
        if (dataType.getRefEntity() != null)
            this.entityInfo = new EntityInfo(this.dataType.getRefEntity(), prefix.length() == 0 ? fieldName : prefix + "." + fieldName, fieldViews);
    }

    public DataClass getDataClass() {
        return this.dataType.getDataClass();
    }

    public ScalarDataType getType() {
        return this.dataType.getType();
    }

    public Enumeration getEnumeration() {
        return this.dataType.getEnumeration();
    }

    public EntityInfo getRefEntity() {
        return this.entityInfo;
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
}
