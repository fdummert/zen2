package de.zeos.db.mongo;

import java.util.Map;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import de.zeos.conversion.Converter;
import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.data.EntityInfo;
import de.zeos.zen2.data.FieldInfo;

public class MapToDBObjectConverter implements Converter<Map<String, Object>, DBObject> {

    @SuppressWarnings("unchecked")
    public DBObject convert(Map<String, Object> sourceObject, Object... context) {
        BasicDBObject dbObject = new BasicDBObject();
        for (String key : sourceObject.keySet()) {
            Object value = sourceObject.get(key);

            // FIXME: extract as conversion plugin
            EntityInfo entityInfo = (EntityInfo) context[0];
            FieldInfo fieldInfo = entityInfo.getField(key);

            EntityInfo refEntityInfo = null;
            if (fieldInfo.getType().getDataClass() == DataClass.ENTITY) {
                refEntityInfo = fieldInfo.getType().resolveRefEntity();
                if (value != null && !fieldInfo.getType().isLazy()) {
                    if (value instanceof Map) {
                        // FIXME: datatype should be declared as string or
                        // real (builtin) ref
                        Map<String, Object> refMap = (Map<String, Object>) value;
                        Object id = refMap.get(refEntityInfo.getPkFieldName());
                        if (id instanceof String) {
                            id = new ObjectId((String) id);
                            refMap.put(refEntityInfo.getPkFieldName(), id);
                        }
                    }
                }
            }

            if (value instanceof Map) {
                value = convert((Map<String, Object>) value, refEntityInfo);
            }
            dbObject.put(key, value);
        }
        return dbObject;
    }
}
