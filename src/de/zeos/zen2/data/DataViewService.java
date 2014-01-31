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
import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.DataView.CommandMode;
import de.zeos.zen2.app.model.DataViewScriptHandler;
import de.zeos.zen2.app.model.DataViewScriptHandler.TriggerMode;
import de.zeos.zen2.app.model.DataViewScriptHandler.TriggerPoint;
import de.zeos.zen2.app.model.ScriptHandlerError;
import de.zeos.zen2.db.DBAccessor;
import de.zeos.zen2.db.InternalDBAccessor;
import de.zeos.zen2.script.ScriptHandlerConsole;

@Service("dv")
@Component
public class DataViewService {

    private class InterruptedException extends Exception {
        private static final long serialVersionUID = -887441181042875126L;
        private Object result;

        public InterruptedException(Object result) {
            this.result = result;
        }

        public Object getResult() {
            return this.result;
        }
    }

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
        String dbApp = app;
        if (scope != null && app.equals(ApplicationRegistry.ZEN2) && view.isSystem() && view.getScope() != null && view.getScope().equals("application")) {
            dbApp = scope;
        }
        DBAccessor accessor = this.appRegistry.getDBAccessor(dbApp);
        Map<String, Object> data = message.getDataAsMap();
        CommandMode mode = CommandMode.valueOf((String) data.get("mode"));

        Map<String, Object> res = new HashMap<>();
        res.put("requestId", data.get("requestId"));
        try {
            HashMap<TriggerMode, HashMap<TriggerPoint, DataViewScriptHandler>> availableHandlers = new HashMap<>();
            for (DataViewScriptHandler handler : internalAccessor.getScriptHandlers(view)) {
                for (TriggerMode m : handler.getTriggerModes()) {
                    HashMap<TriggerPoint, DataViewScriptHandler> triggerPoints = availableHandlers.get(m);
                    if (triggerPoints == null) {
                        triggerPoints = new HashMap<>();
                        availableHandlers.put(m, triggerPoints);
                    }
                    triggerPoints.put(handler.getTriggerPoint(), handler);
                }
            }

            ScriptEngineFacade engine = null;
            engine = processHandler(internalAccessor, dbApp, TriggerPoint.BEFORE_PROCESSING, TriggerMode.ALL, availableHandlers, engine, data, null);

            if (!view.getAllowedModes().contains(mode))
                throw new IllegalStateException("errDataViewModeNotAllowed");

            engine = processHandler(internalAccessor, dbApp, TriggerPoint.BEFORE, TriggerMode.ALL, availableHandlers, engine, data, null);

            @SuppressWarnings("unchecked")
            Map<String, Object> criteria = (Map<String, Object>) data.get("criteria");
            ModelInfo modelInfo = new ModelInfo();
            DataViewInfo dataViewInfo = new DataViewInfo(modelInfo, internalAccessor, view);
            EntityInfo entityInfo = dataViewInfo.getEntity();
            Object result = null;
            switch (mode) {
                case CREATE:
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.BEFORE_PROCESSING, TriggerMode.CREATE, availableHandlers, engine, data, null);
                    filterCriteria(criteria, dataViewInfo, true, true);
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.BEFORE, TriggerMode.CREATE, availableHandlers, engine, criteria, null);
                    result = accessor.insert(criteria, entityInfo);
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.AFTER, TriggerMode.CREATE, availableHandlers, engine, criteria, result);
                    break;
                case READ:
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.BEFORE_PROCESSING, TriggerMode.READ, availableHandlers, engine, data, null);
                    filterCriteria(criteria, dataViewInfo, false, false);
                    Integer pageFrom = (Integer) data.get("pageFrom");
                    Integer pageTo = (Integer) data.get("pageTo");
                    String[] sorts = (String[]) data.get("sorts");
                    Integer count = null;
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.BEFORE, TriggerMode.READ, availableHandlers, engine, criteria, null);
                    List<Map<String, Object>> rows = accessor.select(criteria, pageFrom, pageTo, sorts, entityInfo);
                    if (pageFrom != null || pageTo != null) {
                        count = new Long(accessor.count(criteria, entityInfo)).intValue();
                        res.put("pageFrom", pageFrom);
                        res.put("pageTo", pageFrom + (rows == null ? 0 : rows.size() - 1));
                        res.put("count", count);
                    }
                    result = rows;
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.AFTER, TriggerMode.READ, availableHandlers, engine, criteria, result);
                    break;
                case UPDATE:
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.BEFORE_PROCESSING, TriggerMode.UPDATE, availableHandlers, engine, data, null);
                    filterCriteria(criteria, dataViewInfo, true, true);
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.BEFORE, TriggerMode.UPDATE, availableHandlers, engine, criteria, null);
                    result = accessor.update(criteria, entityInfo);
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.AFTER, TriggerMode.UPDATE, availableHandlers, engine, criteria, result);
                    break;
                case DELETE:
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.BEFORE_PROCESSING, TriggerMode.DELETE, availableHandlers, engine, data, null);
                    filterCriteria(criteria, dataViewInfo, false, false);
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.BEFORE, TriggerMode.DELETE, availableHandlers, engine, criteria, null);
                    result = accessor.delete(criteria, entityInfo);
                    engine = processHandler(internalAccessor, dbApp, TriggerPoint.AFTER, TriggerMode.DELETE, availableHandlers, engine, criteria, result);
                    break;
            }
            engine = processHandler(internalAccessor, dbApp, TriggerPoint.AFTER, TriggerMode.ALL, availableHandlers, engine, criteria, result);
            res.put("result", result);
        } catch (InterruptedException e) {
            res.put("result", e.getResult());
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
        filterCriteria(criteria, dataViewInfo.getEntity(), removeRO, checkMandatory);
    }

    @SuppressWarnings("unchecked")
    private void filterCriteria(Map<String, Object> criteria, EntityInfo entityInfo, boolean removeRO, boolean checkMandatory) throws ValidationException {
        for (Iterator<String> iter = criteria.keySet().iterator(); iter.hasNext();) {
            String crit = iter.next();
            if (!isInternalField(entityInfo.getId(), crit)) {
                FieldInfo f = entityInfo.getField(crit);
                if (f == null || (removeRO && !f.isPk() && f.isReadOnly())) {
                    iter.remove();
                } else if (checkMandatory && f.isMandatory() && criteria.get(crit) == null) {
                    throw new ValidationException(crit, "errMandatory");
                } else if (f.isComplex()) {
                    Object ref = criteria.get(crit);
                    EntityInfo refEntity = f.getType().resolveRefEntity();
                    if (f.getType().getDataClass() == DataClass.ENTITY) {
                        if (ref instanceof Map) {
                            filterCriteria((Map<String, Object>) ref, refEntity, removeRO, checkMandatory);
                        }
                    } else if (f.getType().getDataClass() == DataClass.LIST) {
                        List<Map<String, Object>> list = (List<Map<String, Object>>) ref;
                        for (Map<String, Object> m : list) {
                            filterCriteria(m, refEntity, removeRO, checkMandatory);
                        }
                    }
                }
            }
        }
    }

    private ScriptEngineFacade processHandler(InternalDBAccessor internalAccessor, String app, TriggerPoint point, TriggerMode mode, HashMap<TriggerMode, HashMap<TriggerPoint, DataViewScriptHandler>> availableHandlers, ScriptEngineFacade engine,
            Map<String, Object> data, Object result) throws Exception {
        DataViewScriptHandler handler = null;
        HashMap<TriggerPoint, DataViewScriptHandler> points = availableHandlers.get(mode);
        if (points != null)
            handler = points.get(point);
        if (handler != null) {
            if (!handler.isValid())
                throw new RuntimeException("Invalid dataView handler " + point + " " + mode);
            if (engine == null) {
                engine = this.engineCreator.createEngine();
                engine.activateFeature("dataFeature");
            }
            engine.activateFeature("consoleFeature", new ScriptHandlerConsole(handler, internalAccessor));
            try {
                engine.eval(handler.getSource());
                Invocable invocable = (Invocable) engine;
                try {
                    Object returnedResult = null;
                    if (point == TriggerPoint.BEFORE || point == TriggerPoint.BEFORE_PROCESSING) {
                        DataViewBeforeHandler beforeHandler = invocable.getInterface(DataViewBeforeHandler.class);
                        returnedResult = beforeHandler.process(engine.convertToScriptObject(data), appRegistry.getDBAccessor(app, engine));
                    } else {
                        DataViewAfterHandler afterHandler = invocable.getInterface(DataViewAfterHandler.class);
                        returnedResult = afterHandler.process(engine.convertToScriptObject(data), engine.convertToScriptObject(result), appRegistry.getDBAccessor(app, engine));
                    }
                    if (returnedResult != null)
                        throw new InterruptedException(engine.convertFromScriptObject(returnedResult));
                } catch (UndeclaredThrowableException ex) {
                    throw new ScriptException("DataView handler " + point + " " + mode + " does not implement the process function properly.");
                } catch (Exception ex) {
                    throw engine.convertException(ex);
                }
            } catch (ScriptException ex) {
                ex = (ScriptException) engine.convertException(ex);
                handler.setValid(false);
                internalAccessor.addScriptHandlerError(handler.getId(), new ScriptHandlerError(new Date(), ex.getMessage(), ex.getLineNumber(), ex.getColumnNumber()));
                throw new RuntimeException("DataView handler " + point + " " + mode + " failed.");
            }
        }
        return engine;
    }
}
