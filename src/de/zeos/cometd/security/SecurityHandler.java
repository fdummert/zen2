package de.zeos.cometd.security;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.zeos.cometd.app.ApplicationRegistry;
import de.zeos.script.ScriptEngineCreator;
import de.zeos.script.ScriptEngineFacade;

@Component
public class SecurityHandler {

    @Inject
    private ApplicationRegistry appRegistry;
    @Inject
    private ScriptEngineCreator engineCreator;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Authorization authenticate(String app, Map<String, Object> credentials) throws AuthenticationException {
        String script = appRegistry.getSecurityHandler(app);
        try {
            Digester digester = new Digester("SHA-256", 1024);
            ScriptEngineFacade engine = engineCreator.createEngine();
            engine.put("digester", digester);
            engine.put("credentials", engine.createObject(credentials));
            engine.put("db", appRegistry.getMongoAccessor(app, engine));
            Object ret = engine.eval(script + ";authenticate();");
            logger.debug(ret.toString());
        } catch (Exception ex) {
            logger.warn("Broken script.", ex);
            throw new AuthenticationException();
        }
        return null;
    }
}
