package de.zeos.db.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import de.zeos.conversion.ConversionRegistry;
import de.zeos.conversion.Converter;

public abstract class MapToDBObjectConverter<C> implements Converter<Map<String, Object>, DBObject, C> {

    private ConversionRegistry registry;

    public MapToDBObjectConverter() {
    }

    public MapToDBObjectConverter(ConversionRegistry registry) {
        this.registry = registry;
    }

    public DBObject convert(Map<String, Object> sourceObject, C context) {
        BasicDBObject dbObject = new BasicDBObject();
        for (String key : sourceObject.keySet()) {
            Object value = sourceObject.get(key);
            if (value != null) {
                value = convertValue(sourceObject, key, -1, value, context);
            }
            dbObject.put(key, value);
        }
        return dbObject;
    }

    @SuppressWarnings("unchecked")
    private Object convertValue(Map<String, Object> sourceObject, String key, int idx, Object value, C context) {
        @SuppressWarnings("rawtypes")
        Converter converter = null;
        if (registry != null) {
            converter = registry.getConverter(value.getClass());
            if (converter != null) {
                ConverterContext<Map<String, Object>, C> ctxt = new ConverterContext<Map<String, Object>, C>(sourceObject, key, idx, context);
                value = converter.convert(value, ctxt);
            }
        }
        if (value instanceof Map) {
            value = convert((Map<String, Object>) value, getContext(context, key));
        } else if (value instanceof List) {
            List<Object> orig = (List<Object>) value;
            ArrayList<Object> list = new ArrayList<Object>();
            for (int i = 0; i < orig.size(); i++) {
                list.add(convertValue(sourceObject, key, i, orig.get(i), context));
            }
            value = list;
        }
        return value;
    }

    protected abstract C getContext(C context, String property);
}
