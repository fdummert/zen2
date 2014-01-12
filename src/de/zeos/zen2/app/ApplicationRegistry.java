package de.zeos.zen2.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.zeos.script.ScriptEngineFacade;
import de.zeos.zen2.app.model.Application;
import de.zeos.zen2.app.model.Application.SecurityMode;
import de.zeos.zen2.app.model.DataView.CommandMode;
import de.zeos.zen2.app.model.ScriptHandler;
import de.zeos.zen2.data.EntityInfo;
import de.zeos.zen2.db.DBAccessor;
import de.zeos.zen2.db.DBAccessorFactory;
import de.zeos.zen2.db.DBEvent;
import de.zeos.zen2.db.DBEvent.Type;
import de.zeos.zen2.db.DBListener;
import de.zeos.zen2.db.InternalDBAccessor;

@Component
public class ApplicationRegistry {
    private static String ZEN2 = "zen2";

    public class Console {
        private Logger logger = LoggerFactory.getLogger(getClass());

        public void log(String msg) {
            logger.info(msg);
        }
    }

    private Map<String, Application> applications;
    private Map<String, InternalDBAccessor> internalDBAccessors = new HashMap<>();
    private Map<String, DBAccessor> dbAccessors = new HashMap<>();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private DBAccessorFactory dbAccessorFactory;

    private void startup() {
        try {
            List<Application> apps = getInternalDBAccessor(ZEN2).getApplications();
            if (applications == null)
                applications = new HashMap<>();
            for (Application app : apps) {
                applications.put(app.getId(), app);
            }
        } catch (Exception e) {
            logger.error("Mongo not available", e);
        }
    }

    public Application getApplication(String app) {
        if (this.applications == null) {
            startup();
        }
        return this.applications.get(app);
    }

    private void registerAppEventListener(DBAccessor accessor) {
        accessor.addDBListener(new DBListener() {
            @Override
            public void notify(DBEvent event) {
                if (event.getType() == Type.BEFORE && event.getMode() == CommandMode.UPDATE) {
                    Map<String, Object> query = event.getQuery();
                    SecurityMode mode = SecurityMode.valueOf((String) query.get("securityMode"));
                    if (mode == SecurityMode.PUBLIC)
                        query.put("securityHandler", null);
                } else if (event.getType() == Type.AFTER && event.getMode() != CommandMode.READ) {
                    EntityInfo entityInfo = (EntityInfo) event.getSource();
                    Application app;
                    Map<String, Object> query = event.getQuery();
                    String id = (String) query.get(entityInfo.getPkFieldName());
                    switch (event.getMode()) {
                    case CREATE: {
                        id = (String) event.getResult();
                        app = getInternalDBAccessor(ZEN2).getApplication(id);
                        applications.put(id, app);
                        break;
                    }
                    case DELETE:
                        applications.remove(id);
                        break;
                    case UPDATE:
                        app = getInternalDBAccessor(ZEN2).getApplication(id);
                        applications.put(id, app);
                        break;
                    default:
                    }
                }
            }

            @Override
            public String getEntityName() {
                return "application";
            }
        });
        accessor.addDBListener(new DBListener() {
            @Override
            public void notify(DBEvent event) {
                if (event.getType() == Type.BEFORE && (event.getMode() == CommandMode.CREATE || event.getMode() == CommandMode.UPDATE)) {
                    EntityInfo entityInfo = (EntityInfo) event.getSource();
                    Map<String, Object> query = event.getQuery();
                    Object id = query.get(entityInfo.getPkFieldName());
                    ScriptHandler oldHandler = null;
                    if (id != null)
                        oldHandler = getInternalDBAccessor(event.getApp()).getScriptHandler(id);
                    if (query.get("source") != null && (oldHandler == null || oldHandler.getSource() == null || !oldHandler.getSource().equals(query.get("source"))))
                        query.put("valid", true);
                }
            }

            @Override
            public String getEntityName() {
                return "scriptHandler";
            }
        });
    }

    public DBAccessor getDBAccessor(String app) {
        DBAccessor accessor = this.dbAccessors.get(app);
        if (accessor == null) {
            accessor = dbAccessorFactory.createDBAccessor(app);
            this.dbAccessors.put(app, accessor);
            if (app.equals(ZEN2)) {
                registerAppEventListener(accessor);
            }
        }
        return accessor;
    }

    public DBAccessor getDBAccessor(String app, ScriptEngineFacade facade) {
        DBAccessor accessor = dbAccessorFactory.createScriptableDBAccessor(app, facade);
        if (app.equals(ZEN2)) {
            registerAppEventListener(accessor);
        }
        return accessor;
    }

    public InternalDBAccessor getInternalDBAccessor(String app) {
        InternalDBAccessor accessor = this.internalDBAccessors.get(app);
        if (accessor == null) {
            accessor = dbAccessorFactory.createInternalDBAccessor(app);
            this.internalDBAccessors.put(app, accessor);
        }
        return accessor;
    }
}
