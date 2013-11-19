package de.zeos.cometd.app;

import java.util.Date;
import java.util.Map;

public class MapToScriptObjectConvertor {
    private ScriptEngineFacade engine;

    public MapToScriptObjectConvertor(ScriptEngineFacade engine) {
        this.engine = engine;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createScriptObject(Map<String, Object> sourceObject) {
        Map<String, Object> destObject = engine.createObject();
        for (String key : sourceObject.keySet()) {
            Object value = sourceObject.get(key);
            Object convertedValue = value;
            if (value instanceof Map) {
                convertedValue = convert((Map<String, Object>) value);
            } else if (value instanceof Object[]) {
                convertedValue = convert((Object[]) value);
            } else if (value instanceof long[]) {
                convertedValue = convert((long[]) value);
            } else if (value instanceof int[]) {
                convertedValue = convert((int[]) value);
            } else if (value instanceof short[]) {
                convertedValue = convert((short[]) value);
            } else if (value instanceof double[]) {
                convertedValue = convert((double[]) value);
            } else if (value instanceof float[]) {
                convertedValue = convert((float[]) value);
            } else if (value instanceof boolean[]) {
                convertedValue = convert((boolean[]) value);
            } else if (value instanceof String[]) {
                convertedValue = convert((String[]) value);
            } else if (value instanceof Date[]) {
                convertedValue = convert((Date[]) value);
            }
            destObject.put(key, convertedValue);
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

    private Object convert(Map<String, Object> sourceObject) {
        return createScriptObject(sourceObject);
    }
}
