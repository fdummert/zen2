package de.zeos.zen2.db.mongo;

import javax.inject.Inject;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.Mongo;

import de.zeos.script.ScriptEngineFacade;
import de.zeos.zen2.app.ScriptMongoAccessor;
import de.zeos.zen2.db.DBAccessor;
import de.zeos.zen2.db.DBAccessorFactory;
import de.zeos.zen2.db.InternalDBAccessor;

public class MongoAccessorFactory implements DBAccessorFactory {
    @Inject
    private Mongo mongo;

    @Override
    public DBAccessor createScriptableDBAccessor(String app, ScriptEngineFacade facade) {
        return new ScriptMongoAccessor(new SimpleMongoDbFactory(mongo, app), facade);
    }

    @Override
    public DBAccessor createDBAccessor(String app) {
        return new MongoAccessor(new SimpleMongoDbFactory(mongo, app));
    }

    @Override
    public InternalDBAccessor createInternalDBAccessor(String app) {
        return new MongoInternalDBAccessor(new MongoTemplate(mongo, app));
    }
}
