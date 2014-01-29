package de.zeos.script;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptEngine;

public interface ScriptEngineFacade extends ScriptEngine, Compilable, Invocable {

    public Object convertToScriptObject(Object o);

    public Map<String, Object> convertToScriptObject(Map<String, Object> o);

    public List<Object> convertToScriptObject(List<Object> o);

    public Object convertFromScriptObject(Object o);

    public Map<String, Object> convertFromScriptObject(Map<String, Object> o);

    public List<Object> convertFromScriptObject(List<Object> o);

    public Map<String, Object> createScriptObject();

    public Object createArray(Object[] data);

    public Object createArray(long[] data);

    public Object createArray(int[] data);

    public Object createArray(short[] data);

    public Object createArray(double[] data);

    public Object createArray(float[] data);

    public Object createArray(boolean[] data);

    public Object createArray(String[] data);

    public Object createArray(Date[] data);

    public void activateFeature(String name);

    public void activateFeature(String name, Object arg);

    public Exception convertException(Exception e);
}
