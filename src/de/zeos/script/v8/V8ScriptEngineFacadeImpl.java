package de.zeos.script.v8;

import java.io.Reader;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lu.flier.script.JavascriptError;
import lu.flier.script.V8ScriptEngine;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;

import de.zeos.conversion.DefaultConversionRegistry;
import de.zeos.script.MapToScriptObjectConvertor;
import de.zeos.script.ScriptEngineFacade;
import de.zeos.script.ScriptEngineFeature;
import de.zeos.script.ScriptObjectToMapConvertor;

public class V8ScriptEngineFacadeImpl implements ScriptEngineFacade {

    private V8ScriptEngine v8Engine;
    private V8ConversionRegistry registry;
    private Map<String, ScriptEngineFeature<?>> features = new LinkedHashMap<>();

    public V8ScriptEngineFacadeImpl(ScriptEngineManager manager, List<ScriptEngineFeature<?>> features) {
        this.v8Engine = (V8ScriptEngine) manager.getEngineByName("jav8");
        this.registry = new V8ConversionRegistry(v8Engine);
        if (features != null) {
            for (ScriptEngineFeature<?> f : features)
                this.features.put(f.getName(), f);
        }
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

    @Override
    public Exception convertException(Exception e) {
        if (e instanceof ScriptException) {
            ScriptException se = (ScriptException) e;
            if (se.getColumnNumber() == -1 && e.getCause() != null && e.getCause() != e) {
                return convertException((Exception) e.getCause());
            }
        } else if (e instanceof JavascriptError) {
            String msg = e.getMessage();
            int idx = msg.indexOf('@');
            int close = msg.indexOf(')', idx);
            int lineNo = -1;
            int colNo = -1;
            if (idx >= 0 && close >= idx) {
                String test = msg.substring(idx + 1, close);
                String[] pos = test.split(":");
                if (pos.length == 2) {
                    try {
                        lineNo = new Integer(pos[0].trim());
                        colNo = new Integer(pos[1].trim());
                    } catch (NumberFormatException nfe) {
                    }
                }
            }
            return new ScriptException(msg, null, lineNo, colNo);
        } else if (e instanceof RuntimeException) {
            int idx = e.getMessage().indexOf('#');
            int endIdx = e.getMessage().indexOf('#', idx + 1);
            if (idx == 0 && endIdx > idx) {
                String[] parts = e.getMessage().substring(idx + 1, endIdx).split(":");
                if (parts.length == 2) {
                    String className = parts[0];
                    Class<?> clazz = ClassUtils.resolveClassName(className, ClassUtils.getDefaultClassLoader());

                    Exception ex = null;
                    if (parts[1].length() == 0)
                        ex = (Exception) BeanUtils.instantiate(clazz);
                    else {
                        String[] args = parts[1].split(",");
                        Class<?>[] types = new Class<?>[args.length];
                        Arrays.fill(types, String.class);
                        ex = (Exception) BeanUtils.instantiateClass(ClassUtils.getConstructorIfAvailable(clazz, types), (Object[]) args);
                    }
                    if (ex != null)
                        return ex;
                }
            }
        }
        return e;
    }

    @Override
    public void activateFeature(String name) {
        activateFeature(name, null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void activateFeature(String name, Object arg) {
        ScriptEngineFeature f = this.features.get(name);
        f.activate(this, arg);
    }
}
