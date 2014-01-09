package de.zeos.zen2.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.zeos.db.DBAccessor;
import de.zeos.script.ScriptEngineFacade;
import de.zeos.zen2.app.model.Application;
import de.zeos.zen2.db.DBAccessorFactory;
import de.zeos.zen2.db.InternalDBAccessor;

@Component
public class ApplicationRegistry {
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
            List<Application> apps = dbAccessorFactory.createInternalDBAccessor("zen2").getApplications();
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

    public DBAccessor getDBAccessor(String app) {
        DBAccessor accessor = this.dbAccessors.get(app);
        if (accessor == null) {
            accessor = dbAccessorFactory.createDBAccessor(app);
            this.dbAccessors.put(app, accessor);
        }
        return accessor;
    }

    public DBAccessor getDBAccessor(String app, ScriptEngineFacade facade) {
        return dbAccessorFactory.createScriptableDBAccessor(app, facade);
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
