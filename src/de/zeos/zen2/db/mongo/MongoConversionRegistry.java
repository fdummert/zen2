package de.zeos.zen2.db.mongo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import de.zeos.conversion.Converter;
import de.zeos.conversion.DefaultConversionRegistry;
import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.data.EntityInfo;
import de.zeos.zen2.data.FieldInfo;

public class MongoConversionRegistry extends DefaultConversionRegistry {

    public MongoConversionRegistry() {
        putConverter(ObjectId.class, new Converter<ObjectId, String>() {
            @Override
            public String convert(ObjectId source, Object... context) {
                return source.toString();
            }
        });
        putConverter(BasicDBList.class, new Converter<BasicDBList, List<?>>() {
            @Override
            public List<?> convert(BasicDBList source, Object... context) {
                return new ArrayList<Object>(source);
            }
        });
        putConverter(DBRef.class, new Converter<DBRef, Object>() {
            @Override
            public Object convert(DBRef source, Object... context) {
                // DBObject obj = (DBObject) context[0];
                String property = (String) context[1];
                EntityInfo entityInfo = (EntityInfo) context[2];

                FieldInfo fieldInfo = entityInfo.getField(property);
                if (fieldInfo.getType().getDataClass() == DataClass.ENTITY && !fieldInfo.getType().isLazy()) {
                    EntityInfo refEntity = fieldInfo.getType().resolveRefEntity();
                    return source.getDB().getCollection(source.getRef()).findOne(new BasicDBObject(refEntity.getPkFieldName(), source.getId()), getFields(refEntity));
                }
                return source.getId().toString();
            }

            private DBObject getFields(EntityInfo entityInfo) {
                DBObject dbObj = new BasicDBObject();
                for (String fieldName : entityInfo.getFieldNames(false)) {
                    dbObj.put(fieldName, 1);
                }
                return dbObj;
            }
        });
    }
}
