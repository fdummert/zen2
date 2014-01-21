package de.zeos.script.v8;

import java.io.Reader;
import java.util.Date;
import java.util.Map;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lu.flier.script.V8ScriptEngine;
import de.zeos.conversion.DefaultConversionRegistry;
import de.zeos.script.MapToScriptObjectConvertor;
import de.zeos.script.ScriptEngineFacade;
import de.zeos.script.ScriptObjectToMapConvertor;

public class V8ScriptEngineFacadeImpl implements ScriptEngineFacade {

    private V8ScriptEngine v8Engine;
    private V8ConversionRegistry registry;

    public V8ScriptEngineFacadeImpl(ScriptEngineManager manager) {
        this.v8Engine = (V8ScriptEngine) manager.getEngineByName("jav8");
        this.registry = new V8ConversionRegistry(v8Engine);
    }

    @Override
    public Map<String, Object> createObject(Map<String, Object> source) {
        MapToScriptObjectConvertor convertor = new MapToScriptObjectConvertor(this, registry);
        return convertor.convert(source, null);
    }

    @Override
    public Map<String, Object> toPlainMap(Map<String, Object> source) {
        ScriptObjectToMapConvertor convertor = new ScriptObjectToMapConvertor(new DefaultConversionRegistry());
        return convertor.convert(source, null);
    }

    @Override
    public Map<String, Object> createObject() {
        return v8Engine.createObject();
    }

    @Override
    public Object createArray(Object[] data) {
        return v8Engine.createArray(data);
    }

    @Override
    public Object createArray(long[] data) {
        return v8Engine.createArray(data);
    }

    @Override
    public Object createArray(int[] data) {
        return v8Engine.createArray(data);
    }

    @Override
    public Object createArray(short[] data) {
        return v8Engine.createArray(data);
    }

    @Override
    public Object createArray(double[] data) {
        return v8Engine.createArray(data);
    }

    @Override
    public Object createArray(float[] data) {
        return v8Engine.createArray(data);
    }

    @Override
    public Object createArray(boolean[] data) {
        return v8Engine.createArray(data);
    }

    @Override
    public Object createArray(String[] data) {
        return v8Engine.createArray(data);
    }

    @Override
    public Object createArray(Date[] data) {
        return v8Engine.createArray(data);
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return v8Engine.eval(script, context);
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return v8Engine.eval(reader, context);
    }

    @Override
    public Object eval(String script) throws ScriptException {
        return v8Engine.eval(script);
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        return v8Engine.eval(reader);
    }

    @Override
    public Object eval(String script, Bindings n) throws ScriptException {
        return v8Engine.eval(script, n);
    }

    @Override
    public Object eval(Reader reader, Bindings n) throws ScriptException {
        return v8Engine.eval(reader, n);
    }

    @Override
    public void put(String key, Object value) {
        v8Engine.put(key, value);
    }

    @Override
    public Object get(String key) {
        return v8Engine.get(key);
    }

    @Override
    public Bindings getBindings(int scope) {
        return v8Engine.getBindings(scope);
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        v8Engine.setBindings(bindings, scope);
    }

    @Override
    public Bindings createBindings() {
        return v8Engine.createBindings();
    }

    @Override
    public ScriptContext getContext() {
        return v8Engine.getContext();
    }

    @Override
    public void setContext(ScriptContext context) {
        v8Engine.setContext(context);
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return v8Engine.getFactory();
    }

    @Override
    public CompiledScript compile(String script) throws ScriptException {
        return v8Engine.compile(script);
    }

    @Override
    public CompiledScript compile(Reader script) throws ScriptException {
        return v8Engine.compile(script);
    }

    @Override
    public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException {
        return v8Engine.invokeMethod(thiz, name, args);
    }

    @Override
    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
        return v8Engine.invokeFunction(name, args);
    }

    @Override
    public <T> T getInterface(Class<T> clasz) {
        return v8Engine.getInterface(clasz);
    }

    @Override
    public <T> T getInterface(Object thiz, Class<T> clasz) {
        return v8Engine.getInterface(thiz, clasz);
    }

}
