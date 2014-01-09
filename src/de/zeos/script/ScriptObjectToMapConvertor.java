package de.zeos.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.zeos.conversion.ConversionRegistry;
import de.zeos.conversion.Converter;

public class ScriptObjectToMapConvertor implements Converter<Map<String, Object>, Map<String, Object>> {
    private ConversionRegistry registry;

    public ScriptObjectToMapConvertor(ConversionRegistry registry) {
        this.registry = registry;
    }

    public Map<String, Object> convert(Map<String, Object> sourceObject, Object... contexts) {
        if (sourceObject == null)
            return null;
        Map<String, Object> destObject = new HashMap<String, Object>();
        for (String key : sourceObject.keySet()) {
            Object value = sourceObject.get(key);
            if (value != null) {
                value = convertValue(value);
            }
            destObject.put(key, value);
        }
        return destObject;
    }

    @SuppressWarnings("unchecked")
    private Object convertValue(Object value) {
        @SuppressWarnings("rawtypes")
        Converter converter = registry.getConverter(value.getClass());
        if (converter != null) {
            value = converter.convert(value);
        }
        if (value instanceof Map) {
            value = convert((Map<String, Object>) value);
        } else if (value instanceof List) {
            List<Object> orig = (List<Object>) value;
            ArrayList<Object> list = new ArrayList<Object>();
            for (int i = 0; i < orig.size(); i++) {
                list.add(convertValue(orig.get(i)));
            }
            value = list;
        }
        return value;
    }
}
