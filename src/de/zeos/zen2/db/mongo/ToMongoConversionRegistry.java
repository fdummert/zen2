package de.zeos.zen2.db.mongo;

import java.util.Map;

import org.bson.types.ObjectId;

import de.zeos.conversion.Converter;
import de.zeos.conversion.DefaultConversionRegistry;
import de.zeos.db.mongo.ConverterContext;
import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.data.EntityInfo;
import de.zeos.zen2.data.FieldInfo;

public class ToMongoConversionRegistry extends DefaultConversionRegistry {

    @SuppressWarnings("rawtypes")
    public ToMongoConversionRegistry() {
        putConverter(Map.class, new Converter<Map, Map<String, Object>, ConverterContext<Map<String, Object>, EntityInfo>>() {
            @Override
            public Map<String, Object> convert(Map source, ConverterContext<Map<String, Object>, EntityInfo> convContext) {
                EntityInfo entityInfo = convContext.getContext();
                FieldInfo fieldInfo = entityInfo.getField(convContext.getProperty());

                @SuppressWarnings("unchecked")
                Map<String, Object> refMap = (Map<String, Object>) source;

                EntityInfo refEntityInfo = null;
                if (fieldInfo.getType().getDataClass() == DataClass.ENTITY) {
                    refEntityInfo = fieldInfo.getType().resolveRefEntity();
                    if (!refEntityInfo.isEmbeddable() && !fieldInfo.getType().isLazy()) {

                        Object id = refMap.get(refEntityInfo.getPkFieldName());
                        if (id instanceof String) {
                            id = new ObjectId((String) id);
                            refMap.put(refEntityInfo.getPkFieldName(), id);
                        }
                    }
                }
                return refMap;
            }
        });
    }
}
