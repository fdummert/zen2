package de.zeos.zen2.db.mongo;

import javax.inject.Inject;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

import de.zeos.script.ScriptEngineFacade;
import de.zeos.zen2.app.ScriptMongoAccessor;
import de.zeos.zen2.db.DBAccessor;
import de.zeos.zen2.db.DBAccessorFactory;
import de.zeos.zen2.db.InternalDBAccessor;

public class MongoAccessorFactory implements DBAccessorFactory {
    @Inject
    private Mongo mongo;

    private WriteConcern writeConcern;

    public void setWriteConcern(WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
    }

    @Override
    public DBAccessor createScriptableDBAccessor(String app, ScriptEngineFacade facade) {
        return new ScriptMongoAccessor(new SimpleMongoDbFactory(mongo, app), facade);
    }

    @Override
    public DBAccessor createDBAccessor(String app) {
        SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo, app);
        factory.setWriteConcern(this.writeConcern);
        return new MongoAccessor(factory);
    }

    @Override
    public InternalDBAccessor createInternalDBAccessor(String app) {
        MongoTemplate template = new MongoTemplate(mongo, app);
        template.setWriteConcern(writeConcern);
        return new MongoInternalDBAccessor(template);
    }
}
