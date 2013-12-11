package de.zeos.script;

import java.util.Date;
import java.util.Map;

import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptEngine;

public interface ScriptEngineFacade extends ScriptEngine, Compilable, Invocable {
    public Map<String, Object> createObject(Map<String, Object> source);

    public Map<String, Object> createObject();

    public Map<String, Object> toPlainMap(Map<String, Object> source);

    public Object createArray(Object[] data);

    public Object createArray(long[] data);

    public Object createArray(int[] data);

    public Object createArray(short[] data);

    public Object createArray(double[] data);

    public Object createArray(float[] data);

    public Object createArray(boolean[] data);

    public Object createArray(String[] data);

    public Object createArray(Date[] data);
}
