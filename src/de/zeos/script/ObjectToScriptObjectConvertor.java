package de.zeos.script;

import java.util.Date;
import java.util.List;
import java.util.Map;

import de.zeos.conversion.ConversionRegistry;
import de.zeos.conversion.Converter;

public class ObjectToScriptObjectConvertor implements Converter<Object, Object, Void> {
    private ScriptEngineFacade engine;
    private ConversionRegistry registry;

    public ObjectToScriptObjectConvertor(ScriptEngineFacade engine, ConversionRegistry registry) {
        this.engine = engine;
        this.registry = registry;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object convert(Object sourceObject, Void context) {
        if (sourceObject == null)
            return null;
        Converter converter = (Converter) registry.getConverter(sourceObject.getClass());
        if (converter != null) {
            sourceObject = converter.convert(sourceObject, context);
        }
        if (sourceObject instanceof Map) {
            Map<String, Object> orig = (Map<String, Object>) sourceObject;
            Map<String, Object> destObject = engine.createScriptObject();
            for (String key : orig.keySet()) {
                destObject.put(key, convert(orig.get(key), context));
            }
            return destObject;
        } else if (sourceObject instanceof List) {
            List<Object> orig = (List<Object>) sourceObject;
            if (orig.isEmpty())
                return convert(orig.toArray());
            Object val = orig.get(0);
            Object arr = null;
            if (val instanceof String) {
                arr = orig.toArray(new String[orig.size()]);
            } else if (val instanceof Date) {
                arr = orig.toArray(new Date[orig.size()]);
            } else if (val instanceof Map || val instanceof List) {
                Object[] oarr = new Object[orig.size()];
                for (int i = 0; i < orig.size(); i++) {
                    oarr[i] = convert(orig.get(i), context);
                }
                arr = oarr;
            } else {
                arr = orig.toArray();
            }
            return convert(arr, context);
        } else if (sourceObject instanceof Object[]) {
            sourceObject = convert((Object[]) sourceObject);
        } else if (sourceObject instanceof long[]) {
            sourceObject = convert((long[]) sourceObject);
        } else if (sourceObject instanceof int[]) {
            sourceObject = convert((int[]) sourceObject);
        } else if (sourceObject instanceof short[]) {
            sourceObject = convert((short[]) sourceObject);
        } else if (sourceObject instanceof double[]) {
            sourceObject = convert((double[]) sourceObject);
        } else if (sourceObject instanceof float[]) {
            sourceObject = convert((float[]) sourceObject);
        } else if (sourceObject instanceof boolean[]) {
            sourceObject = convert((boolean[]) sourceObject);
        } else if (sourceObject instanceof String[]) {
            sourceObject = convert((String[]) sourceObject);
        } else if (sourceObject instanceof Date[]) {
            sourceObject = convert((Date[]) sourceObject);
        }
        return sourceObject;
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
