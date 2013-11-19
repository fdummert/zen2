package de.zeos.cometd.app.v8;

import java.io.Reader;
import java.util.Date;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lu.flier.script.V8ScriptEngine;
import de.zeos.cometd.app.MapToScriptObjectConvertor;
import de.zeos.cometd.app.ScriptEngineFacade;

public class V8ScriptEngineFacadeImpl implements ScriptEngineFacade {

    private V8ScriptEngine v8Engine;
    private MapToScriptObjectConvertor convertor;

    public V8ScriptEngineFacadeImpl(ScriptEngineManager manager) {
        this.v8Engine = (V8ScriptEngine) manager.getEngineByName("jav8");
        this.convertor = new MapToScriptObjectConvertor(this);
    }

    @Override
    public Map<String, Object> createObject(Map<String, Object> source) {
        return this.convertor.createScriptObject(source);
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

}
