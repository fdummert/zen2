package de.zeos.db.mongo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoExceptionTranslator;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;

import de.zeos.db.DBAccessor;
import de.zeos.db.Ref;

public class MongoAccessor implements DBAccessor {

    private MongoDbFactory factory;
    private MapToDBObjectConverter queryConverter;
    private DBObjectToMapConverter resultConverter;
    private final MongoExceptionTranslator exceptionTranslator = new MongoExceptionTranslator();

    public MongoAccessor(Mongo mongo, String app, String username, String password) {
        this(new SimpleMongoDbFactory(mongo, app, new UserCredentials(username, password)));
    }

    public MongoAccessor(MongoDbFactory factory) {
        this.factory = factory;
        this.resultConverter = new DBObjectToMapConverter(new MongoConversionRegistry(factory));
        this.queryConverter = new MapToDBObjectConverter();
    }

    @Override
    public boolean exists(Map<String, Object> query, String collection) {
        try {
            DBCollection coll = getCollection(collection);
            DBObject result = coll.findOne(queryConverter.convert(query));
            return result != null;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    public Map<String, Object> selectSingle(Map<String, Object> query, String collection, String... joins) {
        try {
            DBCollection coll = getCollection(collection);
            DBObject result = coll.findOne(queryConverter.convert(query));
            if (result == null)
                return null;
            return convert(result, joins);
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public List<Map<String, Object>> select(Map<String, Object> query, Integer pageFrom, Integer pageTo, String[] sortCols, String collection, String... joins) {
        try {
            DBCollection coll = getCollection(collection);
            DBCursor cursor = coll.find(queryConverter.convert(query));
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
                result.add(convert(obj, joins));
            }
            return result;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public long count(Map<String, Object> query, String from) {
        try {
            DBCollection coll = getCollection(from);
            long cnt = coll.count(queryConverter.convert(query));
            return cnt;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public boolean delete(Map<String, Object> query, String pkField, String collection) {
        try {
            DBCollection coll = getCollection(collection);
            WriteResult result = coll.remove(queryConverter.convert(query));
            return result.getError() == null;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> insert(Map<String, Object> query, String pkField, String collection) {
        try {
            Object id = insertInternal(query, pkField, collection);
            query.put(pkField, id);
            return query;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    private Object insertInternal(Map<String, Object> query, String pkField, String collection) {
        try {
            DBCollection coll = getCollection(collection);
            DBObject dbObj = queryConverter.convert(query);
            persistRefs(dbObj, pkField);
            WriteResult result = coll.insert(dbObj);
            if (result.getError() != null)
                return null;
            return dbObj.get(pkField);
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public boolean update(Map<String, Object> query, String pkField, String collection) {
        try {
            DBCollection coll = getCollection(collection);
            DBObject queryObj = queryConverter.convert(query);
            persistRefs(queryObj, pkField);
            DBObject update = new BasicDBObject();
            Object id = queryObj.removeField(pkField);
            update.put("$set", queryObj);
            WriteResult result = coll.update(new BasicDBObject(Collections.singletonMap(pkField, id)), update);
            return result.getError() == null;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    private void persistRefs(DBObject obj, String pkField) {
        for (String key : obj.keySet()) {
            Object value = obj.get(key);
            if (value instanceof Ref) {
                Ref ref = (Ref) value;
                Object id = null;
                // FIXME how to handle oid?
                if (!ref.isLazy()) {
                    Map<String, Object> refObj = ref.getRefObj();
                    if (exists(refObj, ref.getEntity())) {
                        update(refObj, pkField, ref.getEntity());
                        id = refObj.get(pkField);
                    } else {
                        // FIXME: take modeled pk type into account (builtin vs
                        // string)
                        id = insertInternal(refObj, pkField, ref.getEntity());
                    }
                } else {
                    id = ref.getId();
                }
                obj.put(key, new DBRef(factory.getDb(), ref.getEntity(), id));
            }
        }
    }

    protected Map<String, Object> convert(DBObject dbObject, String... joins) {
        return resultConverter.convert(dbObject, (Object) joins);
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
