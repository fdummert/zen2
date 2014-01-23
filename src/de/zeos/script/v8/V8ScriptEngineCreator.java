package de.zeos.script.v8;

import java.util.List;

import javax.script.ScriptEngineManager;

import de.zeos.script.ScriptEngineCreator;
import de.zeos.script.ScriptEngineFacade;
import de.zeos.script.ScriptEngineFeature;

public class V8ScriptEngineCreator implements ScriptEngineCreator {

    private ScriptEngineManager manager = new ScriptEngineManager();
    private List<ScriptEngineFeature<?>> features;

    public void setFeatures(List<ScriptEngineFeature<?>> features) {
        this.features = features;
    }

    @Override
    public ScriptEngineFacade createEngine() {
        return new V8ScriptEngineFacadeImpl(manager, this.features);
    }
}
