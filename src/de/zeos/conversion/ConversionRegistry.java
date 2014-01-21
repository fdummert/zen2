package de.zeos.conversion;

public interface ConversionRegistry {
    public <S extends Object> Converter<S, ? extends Object, ? extends Object> getConverter(Class<S> sourceClass);
}
