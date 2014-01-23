package de.zeos.script;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

public class ScriptEngineFeature<T> implements BeanNameAware {
    private String name;
    private List<Resource> scripts;
    private List<String> loadedScripts = new ArrayList<>();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    private void init() throws IOException {
        for (Resource r : scripts) {
            String content = StreamUtils.copyToString(r.getInputStream(), Charset.defaultCharset());
            loadedScripts.add(content);
            r.getInputStream().close();
        }
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setScripts(List<Resource> scripts) {
        this.scripts = scripts;
    }

    public List<String> getScripts() {
        return loadedScripts;
    }

    public final void activate(ScriptEngine engine, T arg) {
        for (String script : loadedScripts) {
            try {
                engine.eval(script);
            } catch (ScriptException e) {
                logger.error("Init script failed", e);
                throw new RuntimeException(e);
            }
        }
        performActivation(engine, arg);
    }

    protected void performActivation(ScriptEngine engine, T arg) {
    };
}
