package de.zeos.zen2.db.mongo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

import de.zeos.db.mongo.DBObjectToMapConverter;
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
    private MapToDBObjectConverter<EntityInfo> queryConverter = new MapToDBObjectConverter<EntityInfo>(new ToMongoConversionRegistry()) {
        @Override
        protected EntityInfo getContext(EntityInfo context, String property) {
            return resolveEntity(context, property);
        }
    };
    private DBObjectToMapConverter<EntityInfo> resultConverter = new DBObjectToMapConverter<EntityInfo>(new FromMongoConversionRegistry()) {
        @Override
        protected EntityInfo getContext(EntityInfo context, String property) {
            return resolveEntity(context, property);
        };
    };
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
        return selectSingleInternal(dbObj, null, entityInfo) != null;
    }

    public Map<String, Object> selectSingle(Map<String, Object> query, EntityInfo entityInfo) {
        notifyListeners(CommandMode.READ, Type.BEFORE, entityInfo, query, null);
        DBObject resultObj = selectSingleInternal(queryConverter.convert(query, entityInfo), getFields(entityInfo), entityInfo);
        Map<String, Object> result = null;
        if (resultObj != null)
            result = convert(resultObj, entityInfo);
        notifyListeners(CommandMode.READ, Type.AFTER, entityInfo, query, result);
        return result;
    }

    private DBObject selectSingleInternal(DBObject dbObj, DBObject fields, EntityInfo entityInfo) {
        try {
            DBCollection coll = getCollection(entityInfo.getId());
            return coll.findOne(dbObj, fields);
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    private DBObject selectSingleById(Object id, EntityInfo entityInfo) {
        return selectSingleInternal(new BasicDBObject(entityInfo.getPkFieldName(), id), null, entityInfo);
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
            if (notify)
                notifyListeners(CommandMode.DELETE, Type.BEFORE, entityInfo, new DBObjectMapFacade(dbObj), null);
            DBObject dbObjToDel = selectSingleById(dbObj.get(entityInfo.getPkFieldName()), entityInfo);
            boolean success = false;
            if (dbObjToDel != null) {
                deleteCascadeRefs(dbObjToDel, entityInfo);
                WriteResult result = coll.remove(dbObj);
                success = result.getError() == null;
            }
            if (notify)
                notifyListeners(CommandMode.DELETE, Type.AFTER, entityInfo, new DBObjectMapFacade(dbObj), success);
            return success;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    private void deleteCascadeRefs(DBObject obj, EntityInfo entityInfo) {
        for (FieldInfo fi : entityInfo.getFields().values()) {
            EntityInfo refEntity = fi.getType().resolveRefEntity();
            DataClass dc = fi.getType().getDataClass();
            if (fi.isComplex() && !refEntity.isEmbeddable() && fi.getType().isCascade()) {
                Object ref = obj.get(fi.getName());
                if (ref != null) {
                    if (dc == DataClass.ENTITY)
                        deleteCascadeSingle(refEntity, fi.getType().isLazy(), ref);
                    else if (dc == DataClass.LIST) {
                        List<?> list = (List<?>) ref;
                        for (Object o : list) {
                            deleteCascadeSingle(refEntity, fi.getType().isLazy(), o);
                        }
                    }
                }
            }
        }
    }

    private void deleteCascadeSingle(EntityInfo refEntity, boolean lazy, Object ref) {
        DBObject refObj = null;
        if (!lazy) {
            refObj = ((DBRef) ref).fetch();
        } else {
            refObj = new BasicDBObject(refEntity.getPkFieldName(), ref);
        }
        if (refObj != null) {
            deleteInternal(refObj, refEntity, true);
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
            if (notify)
                notifyListeners(CommandMode.CREATE, Type.BEFORE, entityInfo, new DBObjectMapFacade(dbObj), null);
            persistRefs(dbObj, entityInfo, true);
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
            if (notify)
                notifyListeners(CommandMode.UPDATE, Type.BEFORE, entityInfo, new DBObjectMapFacade(queryObj), null);
            persistRefs(queryObj, entityInfo, false);
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

    @SuppressWarnings("unchecked")
    private void persistRefs(DBObject obj, EntityInfo entityInfo, boolean insert) {
        for (FieldInfo fi : entityInfo.getFields().values()) {
            EntityInfo refEntity = fi.getType().resolveRefEntity();
            DataClass dc = fi.getType().getDataClass();
            if (fi.isComplex() && !refEntity.isEmbeddable()) {
                Object ref = obj.get(fi.getName());
                if (dc == DataClass.ENTITY) {
                    if (ref != null) {
                        DBRef dbRef = persistSingleRef(entityInfo, obj, refEntity, fi, ref, insert);
                        obj.put(fi.getName(), dbRef);
                    } else if (fi.getType().isCascade() && !insert) {
                        // ref is null: obtain orig ref from db
                        DBObject objInDB = selectSingleById(obj.get(entityInfo.getPkFieldName()), entityInfo);
                        Object refInDB = objInDB.get(fi.getName());
                        if (refInDB != null) {
                            deleteCascadeSingle(refEntity, fi.getType().isLazy(), refInDB);
                        }
                    }
                } else if (dc == DataClass.LIST) {
                    @SuppressWarnings("rawtypes")
                    List list = (List) ref;
                    if (ref != null && insert) {
                        // insert mode: list given -> persist db list
                        persistList(list, entityInfo, obj, refEntity, fi, insert);
                    } else if (!insert) {
                        // 1) no list given but db list exists -> remove db list
                        if (ref == null && fi.getType().isCascade()) {
                            DBObject objInDB = selectSingleById(obj.get(entityInfo.getPkFieldName()), entityInfo);
                            Object refInDB = objInDB.get(fi.getName());
                            if (refInDB != null) {
                                @SuppressWarnings("rawtypes")
                                List listInDB = (List) refInDB;
                                for (int i = 0; i < listInDB.size(); i++) {
                                    deleteCascadeSingle(refEntity, fi.getType().isLazy(), list.get(i));
                                }
                            }
                        } else if (ref != null) {
                            DBObject objInDB = selectSingleById(obj.get(entityInfo.getPkFieldName()), entityInfo);
                            Object refInDB = objInDB.get(fi.getName());
                            if (refInDB == null) {
                                // 2) list given but db list null -> insert db
                                // list
                                persistList(list, entityInfo, obj, refEntity, fi, insert);
                            } else {
                                // 3) compare list with db list -> insert/update
                                List<DBObject> listInDB = (List<DBObject>) refInDB;
                                HashSet<Object> ids = new HashSet<Object>();
                                for (int i = 0; i < list.size(); i++) {
                                    Object listObj = list.get(i);
                                    DBRef dbRef = persistSingleRef(entityInfo, obj, refEntity, fi, listObj, insert);
                                    ids.add(dbRef.getId());
                                    list.set(i, dbRef);
                                }
                                // -> remove
                                for (DBObject o : listInDB) {
                                    if (!ids.contains(o.get(refEntity.getPkFieldName())))
                                        deleteInternal(o, refEntity, true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void persistList(List list, EntityInfo entityInfo, DBObject obj, EntityInfo refEntity, FieldInfo fi, boolean insert) {
        for (int i = 0; i < list.size(); i++) {
            DBRef dbRef = persistSingleRef(entityInfo, obj, refEntity, fi, list.get(i), insert);
            if (dbRef != null) {
                list.set(i, dbRef);
            }
        }
    }

    private DBRef persistSingleRef(EntityInfo entityInfo, DBObject obj, EntityInfo refEntity, FieldInfo fi, Object ref, boolean insert) {
        Object id = null;
        if (!fi.getType().isLazy()) {
            DBObject refObj = (DBObject) ref;
            id = refObj.get(refEntity.getPkFieldName());
            DBObject refObjId = new BasicDBObject(refEntity.getPkFieldName(), id);
            if (existsInternal(refObjId, refEntity)) {
                if (insert)
                    throw new IllegalStateException("Nonlazy reference does already exist: " + entityInfo.getId() + "." + fi.getName() + " -> " + refEntity.getId() + " with ID " + id);
                if (fi.getType().isCascade())
                    updateInternal(refObj, refEntity, true);
            } else {
                if (!fi.getType().isCascade())
                    throw new IllegalStateException("Noncascading reference does not exist yet: " + entityInfo.getId() + "." + fi.getName() + " -> " + refEntity.getId() + " with ID " + id);
                id = insertInternal(refObj, refEntity, true);
            }
        } else {
            id = ref;
            DBObject refObjId = new BasicDBObject(refEntity.getPkFieldName(), id);
            if (!existsInternal(refObjId, refEntity)) {
                throw new IllegalStateException("Reference does not exist: " + entityInfo.getId() + "." + fi.getName() + " -> " + refEntity.getId() + " with ID " + id);
            }
        }
        return new DBRef(factory.getDb(), refEntity.getId(), id);
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

    private EntityInfo resolveEntity(EntityInfo entity, String property) {
        FieldInfo fieldInfo = entity.getField(property);
        if (fieldInfo.isComplex()) {
            EntityInfo refEntity = fieldInfo.getType().resolveRefEntity();
            return refEntity;
        }
        return entity;
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
