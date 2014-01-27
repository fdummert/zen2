package de.zeos.zen2.data;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptException;

import org.cometd.annotation.Listener;
import org.cometd.annotation.Service;
import org.cometd.annotation.Session;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import de.zeos.script.ScriptEngineCreator;
import de.zeos.script.ScriptEngineFacade;
import de.zeos.zen2.app.ApplicationRegistry;
import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.DataView.CommandMode;
import de.zeos.zen2.app.model.ScriptHandler;
import de.zeos.zen2.app.model.ScriptHandlerError;
import de.zeos.zen2.db.DBAccessor;
import de.zeos.zen2.db.InternalDBAccessor;
import de.zeos.zen2.script.ScriptHandlerConsole;

@Service("dv")
@Component
public class DataViewService {

    @Session
    private ServerSession serverSession;

    @Inject
    private ScriptEngineCreator engineCreator;

    @Inject
    private ApplicationRegistry appRegistry;

    private Map<String, Set<String>> internalFields = new HashMap<>();

    private Logger logger = LoggerFactory.getLogger(getClass());

    public DataViewService() {
        this.internalFields.put("entity", Collections.singleton("_class"));
        this.internalFields.put("enumeration", Collections.singleton("_class"));
        this.internalFields.put("dataView", Collections.singleton("_class"));
    }

    @Listener("/service/**")
    protected void receiveOnDsChannel(ServerSession remote, Mutable message) {
        String channel = message.getChannel();
        String[] parts = channel.substring(1).split("/", 5);
        if (parts.length >= 5 && parts[2].equals("dv") && parts[3].equals("req")) {
            String app = parts[1];
            String view = parts[4];
            String scope = null;
            if (parts.length > 5)
                scope = parts[5];
            process(remote, message, app, view, scope);
        }
    }

    private void process(ServerSession remote, Mutable message, String app, String dataView, String scope) {
        InternalDBAccessor internalAccessor = this.appRegistry.getInternalDBAccessor(app);
        DataView view = internalAccessor.getDataView(dataView);

        DBAccessor accessor = this.appRegistry.getDBAccessor(app);
        Map<String, Object> data = message.getDataAsMap();
        CommandMode mode = CommandMode.valueOf((String) data.get("mode"));

        Map<String, Object> res = new HashMap<>();
        res.put("requestId", data.get("requestId"));
        try {
            if (!view.getAllowedModes().contains(mode))
                throw new IllegalStateException("errDataViewModeNotAllowed");

            ScriptHandler handler = null;//view.getBeforeHandler();
            ScriptEngineFacade engine = null;
            engine = processHandler(app, "before", handler, engine, data);

            @SuppressWarnings("unchecked")
            Map<String, Object> criteria = (Map<String, Object>) data.get("criteria");
            ModelInfo modelInfo = new ModelInfo();
            DataViewInfo dataViewInfo = new DataViewInfo(modelInfo, internalAccessor, view);
            EntityInfo entityInfo = dataViewInfo.getEntity();
            Object result = null;
            switch (mode) {
                case CREATE:
                    filterCriteria(criteria, dataViewInfo, true, true);
                    result = accessor.insert(criteria, entityInfo);
                    break;
                case READ:
                    filterCriteria(criteria, dataViewInfo, false, false);
                    Integer pageFrom = (Integer) data.get("pageFrom");
                    Integer pageTo = (Integer) data.get("pageTo");
                    String[] sorts = (String[]) data.get("sorts");
                    Integer count = null;
                    List<Map<String, Object>> rows = accessor.select(criteria, pageFrom, pageTo, sorts, entityInfo);
                    if (pageFrom != null || pageTo != null) {
                        count = new Long(accessor.count(criteria, entityInfo)).intValue();
                        res.put("pageFrom", pageFrom);
                        res.put("pageTo", pageFrom + (rows == null ? 0 : rows.size() - 1));
                        res.put("count", count);
                    }
                    result = rows;
                    break;
                case UPDATE:
                    filterCriteria(criteria, dataViewInfo, true, true);
                    result = accessor.update(criteria, entityInfo);
                    break;
                case DELETE:
                    filterCriteria(criteria, dataViewInfo, false, false);
                    result = accessor.delete(criteria, entityInfo);
                    break;
            }
            res.put("result", result);
        } catch (IllegalStateException e) {
            res.put("error", e.getMessage());
        } catch (ValidationException e) {
            res.put("validationErrors", Collections.singletonMap(e.getProperty(), e.getMessage()));
        } catch (DuplicateKeyException e) {
            res.put("error", "errDataViewAlreadyExists");
        } catch (Exception e) {
            res.put("error", "errDataViewGeneral");
            logger.error("Exception while accessing database", e);
        }

        String responseChannel = "/service/" + app + "/dv/res/" + dataView;
        if (scope != null)
            responseChannel += "/" + scope;
        remote.deliver(this.serverSession, responseChannel, res, null);
    }

    private boolean isInternalField(String entity, String field) {
        return (this.internalFields.containsKey(entity) && this.internalFields.get(entity).contains(field));
    }

    private void filterCriteria(Map<String, Object> criteria, DataViewInfo dataViewInfo, boolean removeRO, boolean checkMandatory) throws ValidationException {
        for (Iterator<String> iter = criteria.keySet().iterator(); iter.hasNext();) {
            String crit = iter.next();
            if (!isInternalField(dataViewInfo.getEntity().getId(), crit)) {
                FieldInfo f = dataViewInfo.getEntity().getField(crit);
                if (f == null || (removeRO && !f.isPk() && f.isReadOnly())) {
                    iter.remove();
                } else if (checkMandatory && f.isMandatory() && criteria.get(crit) == null)
                    throw new ValidationException(crit, "errMandatory");
            }
        }
    }

    private ScriptEngineFacade processHandler(String app, String type, ScriptHandler handler, ScriptEngineFacade engine, Map<String, Object> data) throws Exception {
        if (handler != null) {
            if (!handler.isValid())
                throw new RuntimeException("Invalid dataView handler of type: " + type);
            if (engine == null) {
                engine = this.engineCreator.createEngine();
                engine.activateFeature("consoleFeature", new ScriptHandlerConsole(handler, appRegistry.getInternalDBAccessor(app)));
                engine.activateFeature("dataFeature");
                try {
                    engine.eval(handler.getSource());
                    Invocable invocable = (Invocable) engine;
                    // FIXME depends on type category
                    DataViewBeforeHandler beforeHandler = invocable.getInterface(DataViewBeforeHandler.class);
                    try {
                        Object result = beforeHandler.process(data, appRegistry.getDBAccessor(app, engine));
                    } catch (UndeclaredThrowableException ex) {
                        throw new ScriptException("DataView handler of type '" + type + "' does not implement the process function properly.");
                    } catch (Exception ex) {
                        throw engine.convertException(ex);
                    }
                } catch (ScriptException ex) {
                    ex = (ScriptException) engine.convertException(ex);
                    handler.setValid(false);
                    handler.getErrors().add(new ScriptHandlerError(new Date(), ex.getMessage(), ex.getLineNumber(), ex.getColumnNumber()));
                    appRegistry.getInternalDBAccessor(app).updateScriptHandler(handler);
                    throw new RuntimeException("DataView handler failed of type: " + type);
                }
            }
        }
        return engine;
    }
}
