package de.zeos.script.v8;

import lu.flier.script.V8Array;
import lu.flier.script.V8Object;
import lu.flier.script.V8ScriptEngine;
import de.zeos.conversion.Converter;
import de.zeos.conversion.DefaultConversionRegistry;

public class V8ConversionRegistry extends DefaultConversionRegistry {

    public V8ConversionRegistry(final V8ScriptEngine engine) {
        putConverter(V8Array[].class, new Converter<V8Array[], V8Array, Void>() {
            @Override
            public V8Array convert(V8Array[] source, Void context) {
                return engine.createArray(source);
            }
        });
        putConverter(V8Object[].class, new Converter<V8Object[], V8Array, Void>() {
            @Override
            public V8Array convert(V8Object[] source, Void contexts) {
                return engine.createArray(source);
            }
        });
    }
}
