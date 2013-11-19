package de.zeos.cometd.app;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRegistry {
    public class Console {
        private Logger logger = LoggerFactory.getLogger(getClass());

        public void log(String msg) {
            logger.info(msg);
        }
    }

    private Map<String, String> securityHandlers = new HashMap<>();

    @PostConstruct
    private void temp() throws ScriptException {
        storeSecurityHandler("zen2", "function authenticate() { console.log('log from js: ' + toString(credentials)); return { 'foo': 1, 'bar': { 'yak': 'mist'}} };");
    }

    public String getSecurityHandler(String app) {
        return this.securityHandlers.get(app);
    }

    public void storeSecurityHandler(String app, String source) throws ScriptException {
        this.securityHandlers.put(app, source);
    }
}
