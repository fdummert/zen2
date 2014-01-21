package de.zeos.script;

import java.util.Date;
import java.util.Map;

import de.zeos.conversion.ConversionRegistry;
import de.zeos.conversion.Converter;

public class MapToScriptObjectConvertor implements Converter<Map<String, Object>, Map<String, Object>, Void> {
    private ScriptEngineFacade engine;
    private ConversionRegistry registry;

    public MapToScriptObjectConvertor(ScriptEngineFacade engine, ConversionRegistry registry) {
        this.engine = engine;
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> convert(Map<String, Object> sourceObject, Void context) {
        Map<String, Object> destObject = engine.createObject();
        for (String key : sourceObject.keySet()) {
            Object value = sourceObject.get(key);
            if (value != null) {
                @SuppressWarnings("rawtypes")
                Converter converter = (Converter) registry.getConverter(value.getClass());
                if (converter != null) {
                    value = converter.convert(value, null);
                }
                if (value instanceof Map) {
                    value = convert((Map<String, Object>) value, null);
                } else if (value instanceof Object[]) {
                    value = convert((Object[]) value);
                } else if (value instanceof long[]) {
                    value = convert((long[]) value);
                } else if (value instanceof int[]) {
                    value = convert((int[]) value);
                } else if (value instanceof short[]) {
                    value = convert((short[]) value);
                } else if (value instanceof double[]) {
                    value = convert((double[]) value);
                } else if (value instanceof float[]) {
                    value = convert((float[]) value);
                } else if (value instanceof boolean[]) {
                    value = convert((boolean[]) value);
                } else if (value instanceof String[]) {
                    value = convert((String[]) value);
                } else if (value instanceof Date[]) {
                    value = convert((Date[]) value);
                }
            }
            destObject.put(key, value);
        }
        return destObject;
    }

    private Object convert(Object[] sourceObject) {
        return engine.createArray(sourceObject);
    }

    private Object convert(long[] sourceObject) {
        return engine.createArray(sourceObject);
    }

    private Object convert(int[] sourceObject) {
        return engine.createArray(sourceObject);
    }

    private Object convert(short[] sourceObject) {
        return engine.createArray(sourceObject);
    }

    private Object convert(double[] sourceObject) {
        return engine.createArray(sourceObject);
    }

    private Object convert(float[] sourceObject) {
        return engine.createArray(sourceObject);
    }

    private Object convert(boolean[] sourceObject) {
        return engine.createArray(sourceObject);
    }

    private Object convert(String[] sourceObject) {
        return engine.createArray(sourceObject);
    }

    private Object convert(Date[] sourceObject) {
        return engine.createArray(sourceObject);
    }
}
