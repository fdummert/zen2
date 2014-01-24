package de.zeos.zen2.db.mongo;

import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.bson.types.ObjectId;

import de.zeos.conversion.Converter;
import de.zeos.conversion.DefaultConversionRegistry;
import de.zeos.db.mongo.ConverterContext;
import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.app.model.PkType;
import de.zeos.zen2.data.EntityInfo;
import de.zeos.zen2.data.FieldInfo;

public class ToMongoConversionRegistry extends DefaultConversionRegistry {

    public ToMongoConversionRegistry() {
        putConverter(String.class, new Converter<String, Object, ConverterContext<Map<String, Object>, EntityInfo>>() {
            @Override
            public Object convert(String source, ConverterContext<Map<String, Object>, EntityInfo> convContext) {
                EntityInfo entityInfo = convContext.getContext();
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
                } else if (fieldInfo.isComplex() && fieldInfo.getType().isLazy()) {
                    EntityInfo refEntity = fieldInfo.getType().resolveRefEntity();
                    if (!refEntity.isEmbeddable()) {
                        FieldInfo refPkField = refEntity.getField(refEntity.getPkFieldName());
                        if (refPkField.getPkType() == PkType.AUTO)
                            return new ObjectId(source);
                    }
                }
                return source;
            }
        });
    }
}
