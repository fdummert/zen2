package de.zeos.zen2.script;

import javax.script.ScriptEngine;

import de.zeos.script.ScriptEngineFeature;

public class ConsoleFeature extends ScriptEngineFeature<ScriptHandlerConsole> {

    @Override
    public void performActivation(ScriptEngine engine, ScriptHandlerConsole arg) {
        engine.put("$console", arg);
    }

}
