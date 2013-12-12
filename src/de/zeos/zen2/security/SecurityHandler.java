package de.zeos.zen2.security;

import java.util.Arrays;
import java.util.Date;
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

import de.zeos.script.ScriptEngineCreator;
import de.zeos.script.ScriptEngineFacade;
import de.zeos.zen2.app.ApplicationRegistry;
import de.zeos.zen2.app.model.Application;
import de.zeos.zen2.app.model.Application.SecurityMode;
import de.zeos.zen2.app.model.SecurityHandlerError;

@Component
public class SecurityHandler {

    @Inject
    private ApplicationRegistry appRegistry;
    @Inject
    private ScriptEngineCreator engineCreator;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Authorization authenticate(String app, Map<String, Object> credentials) throws AuthenticationException {
        Application application = appRegistry.getApplication(app);
        if (application.getSecurityMode() == SecurityMode.PUBLIC) {
            return new Authorization() {
                @Override
                public Map<String, Object> getData() {
                    return null;
                }

                @Override
                public Set<String> getChannels() {
                    return new HashSet<String>(Arrays.asList(AuthSecurityPolicy.SERVICE_CHANNEL + "**", AuthSecurityPolicy.APP_CHANNEL + "**", AuthSecurityPolicy.PUBLIC_CHANNEL + "**"));
                }
            };
        }

        if (!application.isSecurityHandlerValid())
            throw new AuthenticationException();

        Digester digester = new Digester("SHA-256", 1024);
        try {
            ScriptEngineFacade engine = engineCreator.createEngine();
            engine.eval(application.getSecurityHandler());
            Invocable invocable = (Invocable) engine;
            Authenticator authenticator = invocable.getInterface(Authenticator.class);
            final Map<String, Object> auth;
            try {
                auth = engine.toPlainMap(authenticator.authenticate(engine.createObject(credentials), appRegistry.getMongoAccessor(app, engine), digester));
            } catch (Exception ex) {
                if (ex.getMessage().contains("auth.error"))
                    throw new AuthenticationException();
                throw ex;
            }
            if (auth == null)
                throw new ScriptException("Security handler did not return an authentication.");
            final Set<String> channelSet = new HashSet<String>();
            Object o = auth.get("channels");
            if (o != null) {
                if (!(o instanceof List))
                    throw new ScriptException("Authentication channels must be given as a list");
                List<?> channels = (List<?>) o;
                if (!channels.isEmpty()) {
                    for (Object c : channels) {
                        if (!(c instanceof String))
                            throw new ScriptException("Channel list contains invalid element:" + c.toString());
                        channelSet.add((String) c);
                    }
                }
            }
            o = auth.get("data");
            if (!(o instanceof Map))
                throw new ScriptException("Authentication data must be given as JavaScript object");
            @SuppressWarnings("unchecked")
            final Map<String, Object> data = (Map<String, Object>) o;
            return new Authorization() {
                @Override
                public Map<String, Object> getData() {
                    return data;
                }

                @Override
                public Set<String> getChannels() {
                    return channelSet;
                }
            };
        } catch (ScriptException ex) {
            application.setSecurityHandlerValid(false);
            application.getSecurityHandlerErrors().add(new SecurityHandlerError(new Date(), ex.getMessage(), ex.getLineNumber(), ex.getColumnNumber()));
            throw new AuthenticationException();
        } catch (Exception ex) {
            logger.warn("General error", ex);
            throw new AuthenticationException();
        }
    }
}
