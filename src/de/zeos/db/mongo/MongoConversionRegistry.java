package de.zeos.db.mongo;

import org.bson.types.ObjectId;

import de.zeos.conversion.Converter;
import de.zeos.conversion.DefaultConversionRegistry;

public class MongoConversionRegistry extends DefaultConversionRegistry {

    public MongoConversionRegistry() {
        putConverter(ObjectId.class, new Converter<ObjectId, String>() {
            @Override
            public String convert(ObjectId source) {
                return source.toString();
            }
        });
    }
}
