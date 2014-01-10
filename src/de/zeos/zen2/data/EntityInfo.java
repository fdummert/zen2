package de.zeos.zen2.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.app.model.Entity;
import de.zeos.zen2.app.model.Field;
import de.zeos.zen2.app.model.FieldView;

public class EntityInfo {
    private Entity entity;
    private String prefix;
    private LinkedHashMap<String, FieldInfo> fields = new LinkedHashMap<>();
    private String pkFieldName;

    public EntityInfo(Entity entity, String prefix, List<FieldView> fieldViews) {
        this.entity = entity;
        this.prefix = prefix;
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
                this.fields.put(f.getName(), new FieldInfo(f, fieldView, prefix, fieldViews));
            }
        }
    }

    public EntityInfo(Entity entity, List<FieldView> fields) {
        this(entity, "", fields);
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

    public Collection<FieldInfo> getFields() {
        return this.fields.values();
    }

    public List<String> getFieldNames() {
        ArrayList<String> fields = new ArrayList<>();
        String p = this.prefix;
        if (p.length() > 0)
            p = p + ".";
        for (FieldInfo fi : getFields()) {
            fields.add(p + fi.getName());
            if (fi.getType().getDataClass() == DataClass.ENTITY) {
                fields.addAll(fi.getType().getRefEntity().getFieldNames());
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
            return fv.getType().getRefEntity().getField(path);
        }
        return fields.get(path);
    }

}