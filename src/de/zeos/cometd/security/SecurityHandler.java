package de.zeos.cometd.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private interface InternalAuthorization {
        public List<String> getChannels();
    }

    public Authorization authenticate(String app, Map<String, Object> credentials) throws AuthenticationException {
        String script = appRegistry.getSecurityHandler(app);
        if (script != null) {
            Digester digester = new Digester("SHA-256", 1024);
            try {
                ScriptEngineFacade engine = engineCreator.createEngine();
                engine.eval(script);
                Invocable invocable = (Invocable) engine;
                Authenticator authenticator = invocable.getInterface(Authenticator.class);
                final Map<String, Object> auth = authenticator.authenticate(credentials, appRegistry.getMongoAccessor(app, engine), digester);
                InternalAuthorization authorization = invocable.getInterface(auth, InternalAuthorization.class);
                List<String> channels = authorization.getChannels();
                final Set<String> channelSet = new HashSet<String>(channels);
                return new Authorization() {
                    @Override
                    public Map<String, Object> getData() {
                        return auth;
                    }

                    @Override
                    public Set<String> getChannels() {
                        return channelSet;
                    }
                };
            } catch (ScriptException ex) {
                logger.warn("Script error.", ex);
                throw new AuthenticationException();
            } catch (Exception ex) {
                logger.warn("General error", ex);
                throw new AuthenticationException();
            }
        }
        return new Authorization() {
            @Override
            public Map<String, Object> getData() {
                return null;
            }

            @Override
            public Set<String> getChannels() {
                return Collections.emptySet();
            }
        };
    }
}
