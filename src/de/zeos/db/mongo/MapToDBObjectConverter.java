package de.zeos.db.mongo;

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

    @SuppressWarnings("unchecked")
    public DBObject convert(Map<String, Object> sourceObject, C context) {
        BasicDBObject dbObject = new BasicDBObject();
        for (String key : sourceObject.keySet()) {
            Object value = sourceObject.get(key);
            if (value != null) {
                @SuppressWarnings("rawtypes")
                Converter converter = null;
                if (registry != null) {
                    Class<?> valueClass = value.getClass();
                    if (Map.class.isAssignableFrom(valueClass))
                        valueClass = Map.class;
                    converter = registry.getConverter(valueClass);
                    if (converter != null) {
                        ConverterContext<Map<String, Object>, C> ctxt = new ConverterContext<Map<String, Object>, C>(sourceObject, key, context);
                        value = converter.convert(value, ctxt);
                    }
                }
                if (value instanceof Map) {
                    value = convert((Map<String, Object>) value, getContext(context, key));
                }
            }
            dbObject.put(key, value);
        }
        return dbObject;
    }

    protected abstract C getContext(C context, String property);
}
