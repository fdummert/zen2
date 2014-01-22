package de.zeos.db.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.DBObject;

import de.zeos.conversion.ConversionRegistry;
import de.zeos.conversion.Converter;

public abstract class DBObjectToMapConverter<C> implements Converter<DBObject, Map<String, Object>, C> {

    private ConversionRegistry registry;

    public DBObjectToMapConverter() {
    }

    public DBObjectToMapConverter(ConversionRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> convert(DBObject result, C context) {
        Map<String, Object> map = result.toMap();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value != null) {
                Object convertedValue = convertValue(result, key, -1, value, context);
                if (convertedValue != value)
                    map.put(key, convertedValue);
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private Object convertValue(DBObject sourceObject, String key, int idx, Object value, C context) {
        @SuppressWarnings("rawtypes")
        Converter converter = null;
        if (registry != null) {
            converter = registry.getConverter(value.getClass());
            if (converter != null) {
                ConverterContext<DBObject, C> ctxt = new ConverterContext<DBObject, C>(sourceObject, key, idx, context);
                value = converter.convert(value, ctxt);
            }
        }
        if (value instanceof DBObject) {
            value = convert((DBObject) value, getContext(context, key));
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