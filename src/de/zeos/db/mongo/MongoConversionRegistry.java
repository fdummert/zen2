package de.zeos.db.mongo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDbFactory;

import com.mongodb.DBRef;

import de.zeos.conversion.Converter;
import de.zeos.conversion.DefaultConversionRegistry;

public class MongoConversionRegistry extends DefaultConversionRegistry {

    public MongoConversionRegistry(MongoDbFactory factory) {
        putConverter(ObjectId.class, new Converter<ObjectId, String>() {
            @Override
            public String convert(ObjectId source, Object... context) {
                return source.toString();
            }
        });
        putConverter(DBRef.class, new Converter<DBRef, Object>() {
            @Override
            public Object convert(DBRef source, Object... context) {
                String key = (String) context[1];
                String[] joins = (String[]) context[2];
                for (String s : joins) {
                    if (s.equals(key)) {
                        return source.fetch();
                    }
                }
                return source.getId().toString();
            }
        });
    }
}
