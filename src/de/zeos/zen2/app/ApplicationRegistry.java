package de.zeos.zen2.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import de.zeos.db.MongoAccessor;
import de.zeos.script.ScriptEngineFacade;
import de.zeos.zen2.app.model.Application;

@Component
public class ApplicationRegistry {
    public class Console {
        private Logger logger = LoggerFactory.getLogger(getClass());

        public void log(String msg) {
            logger.info(msg);
        }
    }

    private Map<String, Application> applications;

    //@Inject
    //private Mongo mongo;
    @Inject
    private MongoDbFactory dbFactory;

    @Inject
    private MongoOperations mongoOperations;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private void startup() {
        try {
            List<Application> apps = mongoOperations.findAll(Application.class);
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

    public MongoAccessor getMongoAccessor(String app, ScriptEngineFacade facade) {
        if (app.equals("zen2")) {
            return new ScriptMongoAccessor(dbFactory, facade);
        }
        throw new UnsupportedOperationException("No db access available yet");
    }
}
