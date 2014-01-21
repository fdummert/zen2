package de.zeos.conversion;

public interface Converter<S, D, C> {
    public D convert(S source, C context);
}
