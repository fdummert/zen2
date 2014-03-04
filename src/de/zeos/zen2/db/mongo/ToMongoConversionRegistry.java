package de.zeos.zen2.db.mongo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
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
                } else if (fieldInfo.getType().getDataClass() == DataClass.BINARY) {
                    source = source.substring(source.indexOf(",") + 1);
                    ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes());
                    InputStream b64is;
                    try {
                        b64is = MimeUtility.decode(bais, "base64");
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                    byte[] tmp = new byte[source.length()];
                    int n = 0;
                    try {
                        n = b64is.read(tmp);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    byte[] res = new byte[n];
                    System.arraycopy(tmp, 0, res, 0, n);
                    return res;
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
