package de.zeos.conversion;

import java.util.HashMap;

public class DefaultConversionRegistry implements ConversionRegistry {

    @SuppressWarnings("rawtypes")
    private HashMap<Class, Converter> converters = new HashMap<>();

    public <S> void putConverter(Class<S> clazz, Converter<S, ? extends Object, ? extends Object> converter) {
        converters.put(clazz, converter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> Converter<S, ? extends Object, ? extends Object> getConverter(Class<S> sourceClass) {
        return converters.get(sourceClass);
    }
}
