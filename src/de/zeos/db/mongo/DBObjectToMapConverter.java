package de.zeos.db.mongo;

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
                Object convertedValue = value;

                @SuppressWarnings("rawtypes")
                Converter converter = null;
                if (registry != null) {
                    converter = registry.getConverter(value.getClass());
                    if (converter != null) {
                        ConverterContext<DBObject, C> ctxt = new ConverterContext<DBObject, C>(result, key, context);
                        convertedValue = converter.convert(convertedValue, ctxt);
                    }
                }
                if (convertedValue instanceof DBObject) {
                    convertedValue = convert((DBObject) convertedValue, getContext(context, key));
                }
                if (convertedValue != value)
                    map.put(key, convertedValue);
            }
        }
        return map;
    }

    protected abstract C getContext(C context, String property);
}