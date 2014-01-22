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
import de.zeos.db.mongo.ConverterContext;
import de.zeos.zen2.data.EntityInfo;
import de.zeos.zen2.data.FieldInfo;

public class FromMongoConversionRegistry extends DefaultConversionRegistry {

    public FromMongoConversionRegistry() {
        putConverter(ObjectId.class, new Converter<ObjectId, String, ConverterContext<?, ?>>() {
            @Override
            public String convert(ObjectId source, ConverterContext<?, ?> context) {
                return source.toString();
            }
        });
        putConverter(BasicDBList.class, new Converter<BasicDBList, List<?>, ConverterContext<?, ?>>() {
            @Override
            public List<?> convert(BasicDBList source, ConverterContext<?, ?> context) {
                return new ArrayList<Object>(source);
            }
        });
        putConverter(DBRef.class, new Converter<DBRef, Object, ConverterContext<DBObject, EntityInfo>>() {
            @Override
            public Object convert(DBRef source, ConverterContext<DBObject, EntityInfo> context) {
                String property = context.getProperty();
                EntityInfo entityInfo = context.getContext();

                FieldInfo fieldInfo = entityInfo.getField(property);
                if (!fieldInfo.getType().isLazy() && fieldInfo.isComplex()) {
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
