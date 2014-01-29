package de.zeos.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.zeos.conversion.ConversionRegistry;
import de.zeos.conversion.Converter;

public class ScriptObjectToObjectConvertor implements Converter<Object, Object, Void> {
    private ConversionRegistry registry;

    public ScriptObjectToObjectConvertor(ConversionRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object convert(Object sourceObject, Void context) {
        if (sourceObject == null)
            return null;
        Converter converter = registry.getConverter(sourceObject.getClass());
        if (converter != null)
            sourceObject = converter.convert(sourceObject, context);
        if (sourceObject instanceof Map) {
            Map<String, Object> orig = (Map<String, Object>) sourceObject;
            Map<String, Object> destObject = new HashMap<String, Object>();
            for (String key : orig.keySet()) {
                destObject.put(key, convert(orig.get(key), context));
            }
            return destObject;
        } else if (sourceObject instanceof List) {
            List<Object> orig = (List<Object>) sourceObject;
            ArrayList<Object> list = new ArrayList<Object>();
            for (int i = 0; i < orig.size(); i++) {
                list.add(convert(orig.get(i), context));
            }
            return list;
        }
        return sourceObject;
    }
}
