package de.zeos.cometd.security;

import java.util.Map;

import javax.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptException;

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
        Digester digester = new Digester("SHA-256", 1024);
        try {
            ScriptEngineFacade engine = engineCreator.createEngine();
            engine.put("digester", digester);
            engine.put("credentials", engine.createObject(credentials));
            engine.put("db", appRegistry.getMongoAccessor(app, engine));
            engine.eval(script);
            Invocable invocable = (Invocable) engine;
            Object ret = invocable.invokeFunction("authenticate");
            logger.debug(ret.toString());
        } catch (ScriptException ex) {
            logger.warn("Script error.", ex);
            throw new AuthenticationException();
        } catch (NoSuchMethodException ex) {
            logger.warn("No authenticate method defined.", ex);
            throw new AuthenticationException();
        } catch (Exception ex) {
            logger.warn("General error", ex);
            throw new AuthenticationException();
        }
        return null;
    }
}
