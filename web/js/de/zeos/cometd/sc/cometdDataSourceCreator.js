define(["./cometdDataSource"], function() {
    return function(cm, msgs, dataViewDescriptors) {
        
        var scalarTypeMapping = {
            INT: "integer",
            FLOAT: "float",
            BOOL: "boolean",
            STRING: "text",
            PASSWORD: "password",
            DATE: "date",
            DATETIME: "datetime",
            TIME: "time"
        };
        
        for (var i = 0; i < dataViewDescriptors.length; i++) {
            var descr = dataViewDescriptors[i];
            var props = {
                ID: descr.id + "DS",
                dataView: descr.id,
                messageResolver: function(code) { return msgs[code]; },
                scope: (descr.scope == null ? null : new cm.ApplicationScope(descr.scope)),
                cm: cm
            };
            if (descr.pushable && descr.pushScopes && descr.pushScopes.length > 0) {
                var pushScopes = [];
                for (var j = 0; j < descr.pushScopes.length; j++) {
                    var scope = descr.pushScopes[j];
                    pushScopes.push(new cm.ApplicationScope(scope));
                }
                props.pushScopes = pushScopes;
            }
            var fields = [];
            for (var j = 0; j < descr.entity.fields.length; j++) {
                var skip = false;
                var f = descr.entity.fields[j];
                var field = {
                    name: f.name,
                    title: msgs[descr.entity.id + "_" + f.name] || msgs[f.name]
                };
                if (f.pk === true) field.primaryKey = true;
                if (f.mandatory === true) field.required = true;
                if (f.readOnly === true) field.canEdit = false;
                var type = null;
                switch (f.type.dataClass) {
                case "SCALAR":
                    type = scalarTypeMapping[f.type.type];
                    break;
                case "ENUM":
                    type = "enum";
                    field.valueMap = {};
                    for (var k = 0; k < f.type.enumeration.constants.length; k++) {
                        var cnst = f.type.enumeration.constants[k];
                        field.valueMap[cnst] = msgs[f.type.enumeration.id + "_" + cnst] || cnst;
                    }
                    break;
                case "ENTITY":
                    var embeddable = f.type.refEntity.embeddable;
                    if (embeddable === true) {
                        type = f.type.refEntity._id;
                    } else {
                        field.foreignKey = f.type.refEntity._id;
                    }
                    break;
                case "LIST":
                    if (f.type.type != null || f.type.enumeration != null || (f.type.refEntity != null && f.type.refEntity.embeddable))
                        field.multiple = true;
                    else
                        skip = true;
                    break;
                default:
                    throw "not implemented";
                }
                if (!skip) {
                    field.type = type;
                    fields.push(field);
                }
            }
            props.fields = fields;
            isc.CometDDataSource.create(props);
        }
    }; 
});