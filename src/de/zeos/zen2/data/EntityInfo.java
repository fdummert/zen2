package de.zeos.zen2.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.app.model.Entity;
import de.zeos.zen2.app.model.Field;
import de.zeos.zen2.app.model.FieldView;
import de.zeos.zen2.db.InternalDBAccessor;

public class EntityInfo {
    private ModelInfo modelInfo;
    private DataViewInfo dataViewInfo;
    private Entity entity;
    private String parentEntityId;
    private String prefix;
    private LinkedHashMap<String, FieldInfo> fields = new LinkedHashMap<>();
    private String pkFieldName;

    public EntityInfo(ModelInfo modelInfo, DataViewInfo dataViewInfo, InternalDBAccessor accessor, Entity entity, String prefix, List<FieldView> fieldViews) {
        this.modelInfo = modelInfo;
        this.dataViewInfo = dataViewInfo;
        this.entity = entity;
        modelInfo.addEntity(dataViewInfo, this);
        this.prefix = prefix;

        this.parentEntityId = entity.getParentEntityId();
        if (parentEntityId != null) {
            EntityInfo parentEntityInfo = modelInfo.getEntity(dataViewInfo.getId(), parentEntityId);
            if (parentEntityInfo == null) {
                parentEntityInfo = new EntityInfo(modelInfo, dataViewInfo, accessor, accessor.getEntity(parentEntityId), prefix, fieldViews);
                modelInfo.addEntity(dataViewInfo, parentEntityInfo);
            }
            this.fields.putAll(parentEntityInfo.getFields());
            if (parentEntityInfo.getPkFieldName() != null)
                this.pkFieldName = parentEntityInfo.getPkFieldName();
        }

        boolean processFieldViews = !fieldViews.isEmpty();
        for (Field f : entity.getFields()) {
            if (f.isPk())
                this.pkFieldName = f.getName();
            boolean add = true;
            FieldView fieldView = null;
            if (processFieldViews) {
                for (Iterator<FieldView> iter = fieldViews.iterator(); iter.hasNext();) {
                    FieldView fv = iter.next();
                    if ((prefix + f.getName()).equals(fv.getName())) {
                        fieldView = fv;
                        iter.remove();
                        break;
                    }
                }
                if (fieldView == null)
                    add = false;
            }
            if (add) {
                this.fields.put(f.getName(), new FieldInfo(modelInfo, dataViewInfo, accessor, f, fieldView, prefix, fieldViews));
            }
        }
    }

    public EntityInfo(ModelInfo modelInfo, DataViewInfo dataViewInfo, InternalDBAccessor accessor, Entity entity, List<FieldView> fields) {
        this(modelInfo, dataViewInfo, accessor, entity, "", fields);
    }

    public String getPkFieldName() {
        return this.pkFieldName;
    }

    public String getId() {
        return this.entity.getId();
    }

    public boolean isEmbeddable() {
        return this.entity.isEmbeddable();
    }

    public Map<String, FieldInfo> getFields() {
        return this.fields;
    }

    public List<String> getFieldNames(boolean withPrefix) {
        ArrayList<String> fields = new ArrayList<>();
        String p = "";
        if (withPrefix) {
            p = this.prefix;
            if (p.length() > 0)
                p = p + ".";
        }
        for (FieldInfo fi : getFields().values()) {
            fields.add(p + fi.getName());
            if (fi.getType().getDataClass() == DataClass.ENTITY) {
                EntityInfo info = fi.getType().resolveRefEntity();
                if (info.isEmbeddable())
                    fields.addAll(info.getFieldNames(true));
            }
        }
        return fields;
    }

    public FieldInfo getField(String path) {
        int pos = path.indexOf('.');
        if (pos > 0) {
            String prop = path.substring(0, pos);
            path = path.substring(pos);
            FieldInfo fv = fields.get(prop);
            if (fv.getType().getDataClass() != DataClass.ENTITY)
                return null;
            EntityInfo info = fv.getType().resolveRefEntity();
            return info.getField(path);
        }
        return fields.get(path);
    }

    public String getParentEntityId() {
        return this.parentEntityId;
    }

    public EntityInfo resolveParentEntity() {
        return this.modelInfo.getEntity(this.dataViewInfo.getId(), this.parentEntityId);
    }

}