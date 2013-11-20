package de.zeos.db;

import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import de.zeos.conversion.Converter;

public class MapToDBObjectConverter implements Converter<Map<String, Object>, DBObject> {
    @SuppressWarnings("unchecked")
    public DBObject convert(Map<String, Object> sourceObject) {
        BasicDBObject dbObject = new BasicDBObject();
        for (String key : sourceObject.keySet()) {
            Object value = sourceObject.get(key);
            if (value instanceof Map) {
                value = convert((Map<String, Object>) value);
            }
            dbObject.put(key, value);
        }
        return dbObject;
    }
}
