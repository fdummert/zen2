package de.zeos.db;

import java.util.Map;

import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoExceptionTranslator;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoAccessor {

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

    public Map<String, Object> findOne(Map<String, Object> query, String collection) {
        try {
            DB db = factory.getDb();
            DBCollection coll = db.getCollection(collection);
            DBObject result = coll.findOne(queryConverter.convert(query));
            if (result == null)
                return null;
            return convert(result);
        } catch (RuntimeException e) {
            throw potentiallyConvertRuntimeException(e);
        }
    }

    protected Map<String, Object> convert(DBObject dbObject) {
        return resultConverter.convert(dbObject);
    }

    private RuntimeException potentiallyConvertRuntimeException(RuntimeException ex) {
        RuntimeException resolved = this.exceptionTranslator.translateExceptionIfPossible(ex);
        return resolved == null ? ex : resolved;
    }
}
