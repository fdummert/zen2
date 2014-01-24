package de.zeos.zen2.security;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
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
import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.ScriptHandler;
import de.zeos.zen2.app.model.ScriptHandlerError;
import de.zeos.zen2.db.InternalDBAccessor;
import de.zeos.zen2.script.ScriptHandlerConsole;

@Component
public class SecurityHandler {

    @Inject
    private ApplicationRegistry appRegistry;
    @Inject
    private ScriptEngineCreator engineCreator;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Authorization authenticate(final String app, Map<String, Object> credentials) throws AuthenticationException {
        ScriptHandler handler = null;
        ScriptEngineFacade engine = null;
        try {
            Application application = appRegistry.getApplication(app);
            if (application.getSecurityMode() == SecurityMode.PUBLIC) {
                final List<DataView> dataViews = appRegistry.getInternalDBAccessor(app).getDataViews();
                return createAuthorization(app, null, Collections.singleton("**"), Collections.singleton("**"), dataViews);
            }
            handler = application.getSecurityHandler();
            if (!handler.isValid())
                throw new AuthenticationException();

            Digester digester = new Digester("SHA-256", 1024);
            engine = engineCreator.createEngine();
            engine.activateFeature("consoleFeature", new ScriptHandlerConsole(handler, appRegistry.getInternalDBAccessor(app)));
            engine.activateFeature("authFeature");
            engine.eval(handler.getSource());
            Invocable invocable = (Invocable) engine;
            Authenticator authenticator = invocable.getInterface(Authenticator.class);
            final Map<String, Object> auth;
            try {
                auth = engine.toPlainMap(authenticator.authenticate(engine.createObject(credentials), appRegistry.getDBAccessor(app, engine), digester));
            } catch (UndeclaredThrowableException ex) {
                throw new ScriptException("Security handler does not implement the authenicate function properly.");
            } catch (Exception ex) {
                throw engine.convertException(ex);
            }
            if (auth == null)
                throw new ScriptException("Security handler did not return an authentication.");
            final List<DataView> dataViews = new ArrayList<DataView>();
            Object o = auth.get("dataViews");
            if (o != null) {
                if (!(o instanceof List))
                    throw new ScriptException("Authentication data views must be given as a list");
                List<?> views = (List<?>) o;
                if (!views.isEmpty()) {
                    InternalDBAccessor internalDBAccessor = appRegistry.getInternalDBAccessor(app);
                    for (Object v : views) {
                        if (!(v instanceof String))
                            throw new ScriptException("Data view list contains invalid element:" + v.toString());
                        DataView view = internalDBAccessor.getDataView((String) v);
                        if (view == null)
                            throw new ScriptException("Data view does not exist:" + v.toString());
                        dataViews.add(view);
                    }
                }
            }
            o = auth.get("data");
            if (o != null && !(o instanceof Map))
                throw new ScriptException("Authentication data must be given as JavaScript object");
            @SuppressWarnings("unchecked")
            final Map<String, Object> data = (Map<String, Object>) o;
            return createAuthorization(app, data, collectChannels("publicChannels", auth), collectChannels("appChannels", auth), dataViews);
        } catch (ScriptException ex) {
            ex = (ScriptException) engine.convertException(ex);
            handler.setValid(false);
            handler.getErrors().add(new ScriptHandlerError(new Date(), ex.getMessage(), ex.getLineNumber(), ex.getColumnNumber()));
            appRegistry.getInternalDBAccessor(app).updateScriptHandler(handler);
            throw new AuthenticationException();
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.warn("General error", ex);
            throw new AuthenticationException("errSystem");
        }
    }

    private Set<String> collectChannels(String type, Map<String, Object> auth) throws ScriptException {
        final Set<String> channelSet = new HashSet<String>();
        Object o = auth.get(type);
        if (o != null) {
            if (!(o instanceof List))
                throw new ScriptException("Authentication channels of type '" + type + "' must be given as a list");
            List<?> channels = (List<?>) o;
            if (!channels.isEmpty()) {
                for (Object c : channels) {
                    if (!(c instanceof String))
                        throw new ScriptException("Channel list of type '" + type + "' contains invalid element:" + c.toString());
                    channelSet.add((String) c);
                }
            }
        }
        return channelSet;
    }

    private Authorization createAuthorization(String app, final Map<String, Object> data, final Set<String> publicChannels, final Set<String> appChannels, List<DataView> dataViews) {
        final Set<String> dataViewNames = new HashSet<String>();
        final Set<String> channels = new HashSet<String>();
        for (String c : publicChannels)
            channels.add(AuthSecurityPolicy.PUBLIC_CHANNEL + c);
        for (String c : appChannels)
            channels.add(AuthSecurityPolicy.APP_CHANNEL + app + "/custom/" + c);
        for (DataView v : dataViews) {
            dataViewNames.add(v.getId());
            String prefix = AuthSecurityPolicy.SERVICE_CHANNEL + app + "/dv/";
            String scope = getScope(v.getScope(), data);
            channels.add(prefix + "req/" + v.getId() + scope);
            channels.add(prefix + "res/" + v.getId() + scope);
            if (v.isPushable()) {
                if (v.getPushScopes() != null && v.getPushScopes().size() > 0) {
                    for (String s : v.getPushScopes())
                        channels.add(AuthSecurityPolicy.APP_CHANNEL + app + "/dv/push/" + v.getId() + getScope(s, data));
                } else
                    channels.add(AuthSecurityPolicy.APP_CHANNEL + app + "/dv/push/" + v.getId());
            }
        }
        return new Authorization() {
            @Override
            public Map<String, Object> getData() {
                return data;
            }

            @Override
            public Set<String> getDataViews() {
                return dataViewNames;
            }

            @Override
            public Set<String> getChannels() {
                return channels;
            }

        };
    }

    private String getScope(String prop, Map<String, Object> data) {
        String scope = "";
        if (prop != null) {
            Object d = data.get(prop);
            if (d != null)
                scope = "/" + d.toString();
        }
        return scope;
    }
}
