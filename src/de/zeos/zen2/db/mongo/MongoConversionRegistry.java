package de.zeos.zen2.db.mongo;

import org.bson.types.ObjectId;

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
        putConverter(DBRef.class, new Converter<DBRef, Object>() {
            @Override
            public Object convert(DBRef source, Object... context) {
                // DBObject obj = (DBObject) context[0];
                String property = (String) context[1];
                EntityInfo entityInfo = (EntityInfo) context[2];

                FieldInfo fieldInfo = entityInfo.getField(property);
                if (fieldInfo.getType().getDataClass() == DataClass.ENTITY && !fieldInfo.getType().isLazy())
                    return source.fetch();
                return source.getId().toString();
            }
        });
    }
}
