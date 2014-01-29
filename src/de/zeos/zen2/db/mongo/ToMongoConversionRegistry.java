package de.zeos.zen2.db.mongo;

import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.bson.types.ObjectId;

import com.mongodb.DBRef;

import de.zeos.conversion.Converter;
import de.zeos.conversion.DefaultConversionRegistry;
import de.zeos.db.mongo.ConverterContext;
import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.app.model.PkType;
import de.zeos.zen2.data.EntityInfo;
import de.zeos.zen2.data.FieldInfo;
import de.zeos.zen2.db.mongo.MongoAccessor.EntityInDB;

public class ToMongoConversionRegistry extends DefaultConversionRegistry {

    public ToMongoConversionRegistry() {
        putConverter(String.class, new Converter<String, Object, ConverterContext<Map<String, Object>, EntityInDB>>() {
            @Override
            public Object convert(String source, ConverterContext<Map<String, Object>, EntityInDB> convContext) {
                EntityInfo entityInfo = convContext.getContext().entityInfo;
                FieldInfo fieldInfo = entityInfo.getField(convContext.getProperty());
                if (fieldInfo.getType().getDataClass() == DataClass.SCALAR) {
                    if (fieldInfo.isPk() && fieldInfo.getPkType() == PkType.AUTO) {
                        return new ObjectId(source);
                    }
                    switch (fieldInfo.getType().getType()) {
                        case DATE:
                            return DatatypeConverter.parseDate(source).getTime();
                        case DATETIME:
                            return DatatypeConverter.parseDateTime(source).getTime();
                        case TIME:
                            return DatatypeConverter.parseTime(source).getTime();
                        default:
                    }
                } else if (fieldInfo.isComplex()) {
                    EntityInfo refEntity = fieldInfo.getType().resolveRefEntity();
                    if (!refEntity.isEmbeddable() && fieldInfo.getType().isLazy()) {
                        FieldInfo refPkField = refEntity.getField(refEntity.getPkFieldName());
                        Object id = source;
                        if (refPkField.getPkType() == PkType.AUTO)
                            id = new ObjectId(source);
                        return new DBRef(convContext.getContext().db, refEntity.getId(), id);
                    }
                }
                return source;
            }
        });
    }
}
