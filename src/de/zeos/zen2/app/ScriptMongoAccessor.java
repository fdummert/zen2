package de.zeos.zen2.app;

import java.util.Map;

import org.springframework.data.mongodb.MongoDbFactory;

import com.mongodb.DBObject;

import de.zeos.db.mongo.MongoAccessor;
import de.zeos.script.ScriptEngineFacade;

public class ScriptMongoAccessor extends MongoAccessor {

    private ScriptEngineFacade facade;

    public ScriptMongoAccessor(MongoDbFactory factory, ScriptEngineFacade facade) {
        super(factory);
        this.facade = facade;
    }

    @Override
    protected Map<String, Object> convert(DBObject dbObject) {
        return facade.createObject(super.convert(dbObject));
    }
}
