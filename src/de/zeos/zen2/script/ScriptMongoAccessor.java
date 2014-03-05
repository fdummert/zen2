package de.zeos.zen2.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.MongoDbFactory;

import com.mongodb.DBObject;

import de.zeos.script.ScriptEngineFacade;
import de.zeos.zen2.data.EntityInfo;
import de.zeos.zen2.db.ScriptableDBAccessor;
import de.zeos.zen2.db.mongo.MongoAccessor;

public class ScriptMongoAccessor extends MongoAccessor implements ScriptableDBAccessor {

    private ScriptEngineFacade facade;
    private Map<String, EntityInfo> entities;

    public ScriptMongoAccessor(MongoDbFactory factory, Map<String, EntityInfo> entities, ScriptEngineFacade facade) {
        super(factory);
        this.entities = entities;
        this.facade = facade;
    }

    @Override
    protected Map<String, Object> convert(DBObject dbObject, EntityInfo entityInfo) {
        return facade.convertToScriptObject(super.convert(dbObject, entityInfo));
    }

    @Override
    public boolean exists(Map<String, Object> query, String entityInfo) {
        return exists(query, this.entities.get(entityInfo));
    }

    @Override
    public Map<String, Object> selectSingle(Map<String, Object> query, String entityInfo) {
        return selectSingle(query, this.entities.get(entityInfo), false);
    }

    @Override
    public Map<String, Object> selectSingle(Map<String, Object> query, String entityInfo, boolean includeBinary) {
        return selectSingle(query, this.entities.get(entityInfo), includeBinary);
    }

    @Override
    public List<Object> select(Map<String, Object> query, Integer pageFrom, Integer pageTo, String[] sortCols, String entityInfo) {
        return facade.convertToScriptObject(new ArrayList<Object>(select(query, pageFrom, pageTo, sortCols, this.entities.get(entityInfo))));
    }

    @Override
    public List<Object> select(Map<String, Object> query, String entityInfo) {
        return facade.convertToScriptObject(new ArrayList<Object>(select(query, null, null, null, this.entities.get(entityInfo))));
    }

    @Override
    public long count(Map<String, Object> query, String entityInfo) {
        return count(query, this.entities.get(entityInfo));
    }

    @Override
    public Map<String, Object> insert(Map<String, Object> query, String entityInfo) {
        return insert(query, this.entities.get(entityInfo));
    }

    @Override
    public Map<String, Object> update(Map<String, Object> query, String entityInfo) {
        return update(query, false, this.entities.get(entityInfo));
    }

    @Override
    public Map<String, Object> delete(Map<String, Object> query, String entityInfo) {
        return delete(query, this.entities.get(entityInfo));
    }

}
