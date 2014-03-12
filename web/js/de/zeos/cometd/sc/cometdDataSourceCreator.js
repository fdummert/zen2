define(["./cometdDataSource"], function() {
    return function(cm, msgs, model) {
        
        var scalarTypeMapping = {
            INT: "integer",
            FLOAT: "float",
            BOOL: "boolean",
            STRING: "text",
            PASSWORD: "password",
            DATE: "date",
            DATETIME: "datetime",
            TIME: "time",
            BINARY: "custom"
        };
        
        function deriveDSName(dataViewName) {
            return dataViewName.replaceAll(".", "_");
        }
        
        function createNestedDS(model, dataViewName, refEntity) {
            var nestedDSID = deriveDSName(dataViewName) + "_" + refEntity.id + "DS";
            isc.DataSource.create({
                ID: nestedDSID,
                fields: createDataSourceFields(model, dataViewName, refEntity),
                clientOnly: true
            });
            return nestedDSID;
        }
        
        function createEnum(model, field) {
            var valueMap = {};
            var e = model.enumerations[field.type.enumerationId];
            for (var k = 0; k < e.constants.length; k++) {
                var cnst = e.constants[k];
                valueMap[cnst] = msgs[e.id + "_" + cnst] || cnst;
            }
            return valueMap;
        }
        
        function createDataSourceFields(model, dataViewName, entity) {
            var fields = [];
            for (var fieldName in entity.fields) {
                var skip = false;
                var f = entity.fields[fieldName];
                var field = {
                    name: f.name,
                    title: msgs[entity.id + "_" + f.name] || msgs[f.name]
                };
                if (f.pk === true) {
                    field.primaryKey = true;
                    if (f.pkType == "AUTO") 
                        field.hidden = true;
                    else if (f.pkType == "ASSIGNED") {
                        field.editorType = "PKTextItem";
                    }
                }
                if (f.mandatory === true && f.type.type !== "BOOL") field.required = true;
                if (f.readOnly === true) field.canEdit = false;
                var type = null;
                switch (f.type.dataClass) {
                case "SCALAR":
                    type = scalarTypeMapping[f.type.type];
                    if (f.type.type == "BINARY")
                        field.editorType = "CustomFileItem";
                    break;
                case "BINARY":
                    type = "custom";
                    field.editorType = "CustomFileItem";
                    break;
                case "ENUM":
                    type = "enum";
                    field.valueMap = createEnum(model, f);
                    break;
                case "ENTITY":
                    var refEntity = model.entities[dataViewName + ":" + f.type.refEntityId];
                    if (refEntity.embeddable === true || (f.type.lazy === false && f.type.dataViewId == null)) {
                        type = createNestedDS(model, dataViewName, refEntity);
                        if (refEntity.embeddable === false)
                            field.foreignKey = type + "." + refEntity.pkFieldName;
                        // SC 9.1 fails if value is null instead of undefined
                        field.getFieldValue = function(record, fieldValue, field, fieldName) {
                            var undef;
                            if (fieldValue === null)
                                return undef;
                            return fieldValue;
                        };
                    } else {
                        type = scalarTypeMapping[refEntity.fields[refEntity.pkFieldName].type.type];
                        if (f.type.dataViewId != null) {
                            var dv = model.dataViews[f.type.dataViewId];
                            if (dv == null)
                                throw "specified davaview does not exist";
                            field.foreignKey = dv.id + "DS." + refEntity.pkFieldName;
                        }
                    }
                    break;
                case "LIST":
                    var refEntity = null;
                    if (f.type.refEntityId != null)
                        refEntity = model.entities[dataViewName + ":" + f.type.refEntityId];
                    field.multiple = true;
                    if (f.type.type != null) {
                        type = scalarTypeMapping[f.type.type];
                    } else if (f.type.enumerationId != null) {
                        type = "enum";
                        field.valueMap = createEnum(model, f);
                    } else if (refEntity != null) {
                        if (refEntity.embeddable === true || (f.type.lazy === false && f.type.dataViewId == null)) {
                            type = createNestedDS(model, dataViewName, refEntity);
                        } else if (f.type.dataViewId != null) {
                            var dv = model.dataViews[f.type.dataViewId];
                            if (dv == null)
                                throw "specified davaview does not exist";
                            type = dv.id + "DS";
                            if (f.type.lazy === true && !f.type.inverse)
                                field.foreignKey = type + "." + refEntity.pkFieldName; 
                        } else {
                            skip = true;
                        }
                    }
                    break;
                default:
                    throw "not implemented";
                }
                if (!skip) {
                    field.type = type;
                    fields.push(field);
                }
            }
            return fields;
        }
        
        for (var dataViewName in model.dataViews) {
            var dataView = model.dataViews[dataViewName];
            var props = {
                ID: deriveDSName(dataViewName) + "DS",
                dataView: dataViewName,
                messageResolver: function(code) { return msgs[code]; },
                scope: (dataView.scope == null ? null : new cm.ApplicationScope(dataView.scope)),
                cm: cm
            };
            if (dataView.pushable && dataView.pushScopes && dataView.pushScopes.length > 0) {
                var pushScopes = [];
                for (var j = 0; j < dataView.pushScopes.length; j++) {
                    var scope = dataView.pushScopes[j];
                    pushScopes.push(new cm.ApplicationScope(scope));
                }
                props.pushScopes = pushScopes;
            }
            
            props.fields = createDataSourceFields(model, dataViewName, dataView.entity);
            isc.CometDDataSource.create(props);
        }
        isc.CometDDataSource.model = model;
    }; 
});