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
import com.mongodb.Mongo;
import com.mongodb.WriteResult;

import de.zeos.db.DBAccessor;

public class MongoAccessor implements DBAccessor {

    private MongoDbFactory factory;
    private MapToDBObjectConverter queryConverter = new MapToDBObjectConverter();
    private DBObjectToMapConverter resultConverter = new DBObjectToMapConverter(new MongoConversionRegistry());
    private final MongoExceptionTranslator exceptionTranslator = new MongoExceptionTranslator();

    public MongoAccessor(Mongo mongo, String app, String username, String password) {
        this(new SimpleMongoDbFactory(mongo, app, new UserCredentials(username, password)));
    }

    public MongoAccessor(MongoDbFactory factory) {
        this.factory = factory;
    }

    public Map<String, Object> selectSingle(Map<String, Object> query, String collection) {
        try {
            DBCollection coll = getCollection(collection);
            DBObject result = coll.findOne(queryConverter.convert(query));
            if (result == null)
                return null;
            return convert(result);
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public List<Map<String, Object>> select(Map<String, Object> query, Integer pageFrom, Integer pageTo, String[] sortCols, String collection) {
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
                result.add(convert(obj));
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
    public boolean delete(Map<String, Object> query, String collection) {
        try {
            DBCollection coll = getCollection(collection);
            WriteResult result = coll.remove(queryConverter.convert(query));
            return result.getError() != null;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public boolean insert(Map<String, Object> query, String collection) {
        try {
            DBCollection coll = getCollection(collection);
            WriteResult result = coll.insert(queryConverter.convert(query));
            return result.getError() != null;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    @Override
    public boolean update(Map<String, Object> query, String collection) {
        try {
            DBCollection coll = getCollection(collection);
            WriteResult result = coll.update(new BasicDBObject(Collections.singletonMap("_id", query.get("_id"))), queryConverter.convert(query));
            return result.getError() != null;
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    protected Map<String, Object> convert(DBObject dbObject) {
        return resultConverter.convert(dbObject);
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
