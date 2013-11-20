package de.zeos.conversion;

public interface ConversionRegistry {
    public <S extends Object> Converter<S, ? extends Object> getConverter(Class<S> sourceClass);
}
