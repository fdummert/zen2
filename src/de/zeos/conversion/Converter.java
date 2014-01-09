package de.zeos.conversion;

public interface Converter<S, D> {
    public D convert(S source, Object... contexts);
}
