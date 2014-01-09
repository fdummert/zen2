package de.zeos.db.mongo;

import java.util.Map;

import com.mongodb.DBObject;

import de.zeos.conversion.ConversionRegistry;
import de.zeos.conversion.Converter;

public class DBObjectToMapConverter implements Converter<DBObject, Map<String, Object>> {

    private ConversionRegistry registry;

    public DBObjectToMapConverter() {
    }

    public DBObjectToMapConverter(ConversionRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> convert(DBObject result, Object... contexts) {
        Map<String, Object> map = result.toMap();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value != null) {
                Object convertedValue = value;
                @SuppressWarnings("rawtypes")
                Converter converter = null;
                if (registry != null) {
                    converter = registry.getConverter(value.getClass());
                    if (converter != null) {
                        convertedValue = converter.convert(convertedValue, result, key, contexts[0]);
                    }
                }
                if (value instanceof DBObject) {
                    convertedValue = convert((DBObject) convertedValue, contexts);
                }
                if (convertedValue != value)
                    map.put(key, convertedValue);
            }
        }
        return map;
    }
}