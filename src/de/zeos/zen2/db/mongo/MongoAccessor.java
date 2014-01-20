package de.zeos.zen2.db.mongo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoExceptionTranslator;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.WriteResult;

import de.zeos.db.mongo.MapToDBObjectConverter;
import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.app.model.DataView.CommandMode;
import de.zeos.zen2.data.EntityInfo;
import de.zeos.zen2.data.FieldInfo;
import de.zeos.zen2.db.DBAccessor;
import de.zeos.zen2.db.DBEvent;
import de.zeos.zen2.db.DBEvent.Type;
import de.zeos.zen2.db.DBListener;

public class MongoAccessor implements DBAccessor {

    private MongoDbFactory factory;
    private MapToDBObjectConverter queryConverter = new MapToDBObjectConverter();
    private DBObjectToMapConverter resultConverter = new DBObjectToMapConverter(new MongoConversionRegistry());
    private final MongoExceptionTranslator exceptionTranslator = new MongoExceptionTranslator();

    private HashMap<String, ArrayList<DBListener>> listeners = new HashMap<>();

    public MongoAccessor(MongoDbFactory factory) {
        this.factory = factory;
    }

    @Override
    public void addDBListener(DBListener listener) {
        ArrayList<DBListener> entityListeners = listeners.get(listener.getEntityName());
        if (entityListeners == null) {
            entityListeners = new ArrayList<>();
            listeners.put(listener.getEntityName(), entityListeners);
        }
        entityListeners.add(listener);
    }

    @Override
    public void removeDBListener(DBListener listener) {
        ArrayList<DBListener> entityListeners = listeners.get(listener.getEntityName());
        entityListeners.remove(listener);
        if (entityListeners.isEmpty()) {
            listeners.remove(listener.getEntityName());
        }
    }

    @Override
    public boolean exists(Map<String, Object> query, EntityInfo entityInfo) {
        return existsInternal(queryConverter.convert(query, entityInfo), entityInfo);
    }

    private boolean existsInternal(DBObject dbObj, EntityInfo entityInfo) {
        try {
            DBCollection coll = getCollection(entityInfo.getId());
            DBObject result = coll.findOne(dbObj);
            return result != null;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    public Map<String, Object> selectSingle(Map<String, Object> query, EntityInfo entityInfo) {
        try {
            DBCollection coll = getCollection(entityInfo.getId());
            notifyListeners(CommandMode.READ, Type.BEFORE, entityInfo, query, null);
            DBObject resultObj = coll.findOne(queryConverter.convert(query, entityInfo), getFields(entityInfo));
            Map<String, Object> result = null;
            if (resultObj != null)
                result = convert(resultObj, entityInfo);
            notifyListeners(CommandMode.READ, Type.AFTER, entityInfo, query, result);
            return result;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public List<Map<String, Object>> select(Map<String, Object> query, Integer pageFrom, Integer pageTo, String[] sortCols, EntityInfo entityInfo) {
        try {
            DBCollection coll = getCollection(entityInfo.getId());
            notifyListeners(CommandMode.READ, Type.BEFORE, entityInfo, query, null);
            DBCursor cursor = coll.find(queryConverter.convert(query, entityInfo), getFields(entityInfo));
            if (pageFrom != null)
                cursor = cursor.skip(pageFrom);
            if (pageTo != null)
                cursor = cursor.limit(pageTo - (pageFrom == null ? 0 : pageFrom) + 1);
            if (sortCols != null) {
                BasicDBObject sortObj = new BasicDBObject();
                for (String sortCol : sortCols) {
                    boolean asc = true;
                    if (sortCol.indexOf(0) == '-') {
                        asc = false;
                        sortCol = sortCol.substring(1);
                    }
                    sortObj.put(sortCol, asc ? 1 : -1);
                }
                cursor = cursor.sort(sortObj);
            }
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            while (cursor.hasNext()) {
                DBObject obj = cursor.next();
                result.add(convert(obj, entityInfo));
            }
            notifyListeners(CommandMode.READ, Type.AFTER, entityInfo, query, result);
            return result;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public long count(Map<String, Object> query, EntityInfo entityInfo) {
        try {
            DBCollection coll = getCollection(entityInfo.getId());
            long cnt = coll.count(queryConverter.convert(query, entityInfo));
            return cnt;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> delete(Map<String, Object> query, EntityInfo entityInfo) {
        notifyListeners(CommandMode.DELETE, Type.BEFORE, entityInfo, query, null);
        boolean success = deleteInternal(queryConverter.convert(query, entityInfo), entityInfo, false);
        notifyListeners(CommandMode.DELETE, Type.AFTER, entityInfo, query, success);
        return success ? query : null;
    }

    private boolean deleteInternal(DBObject dbObj, EntityInfo entityInfo, boolean notify) {
        try {
            DBCollection coll = getCollection(entityInfo.getId());
            followRefs(dbObj, entityInfo);
            if (notify)
                notifyListeners(CommandMode.DELETE, Type.BEFORE, entityInfo, new DBObjectMapFacade(dbObj), null);
            WriteResult result = coll.remove(dbObj);
            boolean success = result.getError() == null;
            if (notify)
                notifyListeners(CommandMode.DELETE, Type.AFTER, entityInfo, new DBObjectMapFacade(dbObj), success);
            return success;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> insert(Map<String, Object> query, EntityInfo entityInfo) {
        notifyListeners(CommandMode.CREATE, Type.BEFORE, entityInfo, query, null);
        Object id = insertInternal(queryConverter.convert(query, entityInfo), entityInfo, false);
        query.put(entityInfo.getPkFieldName(), id);
        notifyListeners(CommandMode.CREATE, Type.AFTER, entityInfo, query, id);
        return query;
    }

    private Object insertInternal(DBObject dbObj, EntityInfo entityInfo, boolean notify) {
        try {
            DBCollection coll = getCollection(entityInfo.getId());
            persistRefs(dbObj, entityInfo);
            if (notify)
                notifyListeners(CommandMode.CREATE, Type.BEFORE, entityInfo, new DBObjectMapFacade(dbObj), null);
            WriteResult writeResult = coll.insert(dbObj);
            Object result = null;
            if (writeResult.getError() == null)
                result = dbObj.get(entityInfo.getPkFieldName());
            if (notify)
                notifyListeners(CommandMode.CREATE, Type.AFTER, entityInfo, new DBObjectMapFacade(dbObj), result);
            return result;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> update(Map<String, Object> query, EntityInfo entityInfo) {
        notifyListeners(CommandMode.UPDATE, Type.BEFORE, entityInfo, query, null);
        boolean success = updateInternal(queryConverter.convert(query, entityInfo), entityInfo, false);
        notifyListeners(CommandMode.UPDATE, Type.AFTER, entityInfo, query, success);
        return success ? query : null;
    }

    private boolean updateInternal(DBObject queryObj, EntityInfo entityInfo, boolean notify) {
        try {
            DBCollection coll = getCollection(entityInfo.getId());
            persistRefs(queryObj, entityInfo);
            if (notify)
                notifyListeners(CommandMode.UPDATE, Type.BEFORE, entityInfo, new DBObjectMapFacade(queryObj), null);
            DBObject update = new BasicDBObject();
            Object id = queryObj.removeField(entityInfo.getPkFieldName());
            update.put("$set", queryObj);
            WriteResult result = coll.update(new BasicDBObject(Collections.singletonMap(entityInfo.getPkFieldName(), id)), update);
            boolean success = result.getError() == null;
            if (notify)
                notifyListeners(CommandMode.UPDATE, Type.AFTER, entityInfo, new DBObjectMapFacade(queryObj), success);
            return success;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    private DBObject getFields(EntityInfo entityInfo) {
        DBObject dbObj = new BasicDBObject();
        for (String fieldName : entityInfo.getFieldNames(true)) {
            dbObj.put(fieldName, 1);
        }
        return dbObj;
    }

    private void followRefs(DBObject obj, EntityInfo entityInfo) {
        for (FieldInfo fi : entityInfo.getFields().values()) {
            EntityInfo refEntity = fi.getType().resolveRefEntity();
            if (fi.getType().getDataClass() == DataClass.ENTITY && !refEntity.isEmbeddable() && fi.getType().isCascade()) {
                Object ref = obj.get(fi.getName());
                if (ref != null) {
                    DBObject refObj = null;
                    if (!fi.getType().isLazy()) {
                        refObj = (DBObject) ref;
                        if (!existsInternal(refObj, refEntity)) {
                            refObj = null;
                        }
                    } else {
                        refObj = new BasicDBObject(refEntity.getPkFieldName(), ref);
                    }
                    if (refObj != null) {
                        deleteInternal(refObj, refEntity, true);
                    }
                }
            }
        }
    }

    private void persistRefs(DBObject obj, EntityInfo entityInfo) {
        for (FieldInfo fi : entityInfo.getFields().values()) {
            EntityInfo refEntity = fi.getType().resolveRefEntity();
            if (fi.getType().getDataClass() == DataClass.ENTITY && !refEntity.isEmbeddable()) {
                Object id = null;
                Object ref = obj.get(fi.getName());
                if (ref != null) {
                    if (!fi.getType().isLazy()) {
                        DBObject refObj = (DBObject) ref;
                        // FIXME: check for id only, not complete query!
                        if (existsInternal(refObj, refEntity)) {
                            if (fi.getType().isCascade())
                                updateInternal(refObj, refEntity, true);
                            id = refObj.get(entityInfo.getPkFieldName());
                        } else {
                            if (!fi.getType().isCascade())
                                throw new IllegalStateException("Noncascading reference does not exist yet: " + entityInfo.getId() + "." + fi.getName() + " -> " + refEntity.getId());
                            id = insertInternal(refObj, refEntity, true);
                        }
                    } else {
                        id = ref;
                    }
                    obj.put(fi.getName(), new DBRef(factory.getDb(), refEntity.getId(), id));
                } else if (fi.getType().isCascade()) {
                    DBObject refObj = null;
                    if (!fi.getType().isLazy())
                        refObj = (DBObject) ref;
                    else {
                        refObj = new BasicDBObject(refEntity.getPkFieldName(), ref);
                    }
                    deleteInternal(refObj, refEntity, true);
                }
            }
        }
    }

    private void notifyListeners(CommandMode mode, DBEvent.Type type, EntityInfo entity, Map<String, Object> query, Object result) {
        ArrayList<DBListener> generalListeners = listeners.get(null);
        ArrayList<DBListener> entityListeners = listeners.get(entity.getId());
        if (listeners.get(null) == null && listeners.get(entity.getId()) == null)
            return;
        DBEvent event = new DBEvent(factory.getDb().getName(), entity, mode, type, query, result);
        if (generalListeners != null) {
            for (DBListener l : generalListeners)
                l.notify(event);
        }
        if (entityListeners != null) {
            for (DBListener l : entityListeners)
                l.notify(event);
        }
    }

    protected Map<String, Object> convert(DBObject dbObject, EntityInfo entityInfo) {
        return resultConverter.convert(dbObject, entityInfo);
    }

    private DBCollection getCollection(String collection) {
        DB db = factory.getDb();
        return db.getCollection(collection);
    }

    private RuntimeException potentiallyConvertRuntimeException(RuntimeException ex) {
        RuntimeException resolved = this.exceptionTranslator.translateExceptionIfPossible(ex);
        return resolved == null ? ex : resolved;
    }
}
