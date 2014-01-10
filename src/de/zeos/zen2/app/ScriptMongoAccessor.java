package de.zeos.zen2.app;

import java.util.Map;

import org.springframework.data.mongodb.MongoDbFactory;

import com.mongodb.DBObject;

import de.zeos.script.ScriptEngineFacade;
import de.zeos.zen2.data.EntityInfo;
import de.zeos.zen2.db.mongo.MongoAccessor;

public class ScriptMongoAccessor extends MongoAccessor {

    private ScriptEngineFacade facade;

    public ScriptMongoAccessor(MongoDbFactory factory, ScriptEngineFacade facade) {
        super(factory);
        this.facade = facade;
    }

    @Override
    protected Map<String, Object> convert(DBObject dbObject, EntityInfo entityInfo) {
        return facade.createObject(super.convert(dbObject, entityInfo));
    }
}
