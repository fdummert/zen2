package de.zeos.script.v8;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import de.zeos.script.ScriptEngineCreator;
import de.zeos.script.ScriptEngineFacade;

public class V8ScriptEngineCreator implements ScriptEngineCreator {

    private ScriptEngineManager manager = new ScriptEngineManager();
    private List<Resource> scripts;
    private LinkedHashMap<String, String> loadedScripts = new LinkedHashMap<String, String>();
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void setScripts(List<Resource> scripts) {
        this.scripts = scripts;
    }

    @PostConstruct
    private void init() throws IOException {
        if (scripts == null)
            throw new IllegalArgumentException("No scripts specified");
        for (Resource r : scripts) {
            String content = StreamUtils.copyToString(r.getInputStream(), Charset.defaultCharset());
            loadedScripts.put(r.getFilename(), content);
            r.getInputStream().close();
        }
    }

    @Override
    public ScriptEngineFacade createEngine() {
        ScriptEngineFacade facade = new V8ScriptEngineFacadeImpl(manager);
        for (String file : loadedScripts.keySet())
            try {
                facade.eval(loadedScripts.get(file));
            } catch (ScriptException e) {
                logger.error("Init script failed", e);
            }
        return facade;
    }
}
