db.zen2.enumeration.insert( { _id: "zen2.securityModes", _system: true, constants: [ "PUBLIC", "PROTECTED" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.zen2.enumeration.insert( { _id: "zen2.pkTypes", _system: true, constants: [ "AUTO", "ASSIGNED" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.zen2.enumeration.insert( { _id: "zen2.dataClasses", _system: true, constants: [ "SCALAR", "ENUM", "LIST", "ENTITY", "BINARY" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.zen2.enumeration.insert( { _id: "zen2.scalarDataTypes", _system: true, constants: [ "BOOL", "INT", "FLOAT", "DATE", "DATETIME", "TIME", "PASSWORD", "STRING", "BINARY" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.zen2.enumeration.insert( { _id: "zen2.overwriteModes", _system: true, constants: [ "REDEFINITION", "WHITELIST" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.zen2.enumeration.insert( { _id: "zen2.commandModes", _system: true, constants: [ "CREATE", "READ", "UPDATE", "DELETE" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.zen2.enumeration.insert( { _id: "zen2.triggerModes", _system: true, constants: [ "ALL", "CREATE", "READ", "UPDATE", "DELETE" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.zen2.enumeration.insert( { _id: "zen2.triggerPoints", _system: true, constants: [ "BEFORE_PROCESSING", "BEFORE", "AFTER" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.zen2.enumeration.insert( { _id: "zen2.resourceTypes", _system: true, constants: [ "PNG", "HTML", "JS", "CSS", "CUSTOM_BINARY", "CUSTOM_TEXT" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.zen2.enumeration.insert( { _id: "zen2.templateTypes", _system: true, constants: [ "HTML", "JS" ], _class: "de.zeos.zen2.app.model.Enumeration" } );

db.zen2.entity.insert( { _id: "zen2.application", embeddable: false, _system: true, fields: [
      { name: "_id", pk: true, pkType: "ASSIGNED", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "securityMode", mandatory: true, type: { dataClass: "ENUM", enumerationId: "zen2.securityModes" }}, 
      { name: "securityHandler", type: { dataClass: "ENTITY", refEntityId: "zen2.scriptHandler", lazy: false, cascade: true } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.entity", embeddable: false, _system: true, fields: [
      { name: "_id", pk: true, pkType: "ASSIGNED", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "parentEntityId", type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "embeddable", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" } },
      { name: "_system", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" }, readOnly: true },
      { name: "fields", type: { dataClass: "LIST", refEntityId: "zen2.field" } },
      { name: "indexes", type: { dataClass: "LIST", refEntityId: "zen2.index" } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.field", embeddable: true, _system: true, fields: [
      { name: "name", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "pk", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" } },
      { name: "pkType", type: { dataClass: "ENUM", enumerationId: "zen2.pkTypes" }},
      { name: "mandatory", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" } },
      { name: "readOnly", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" } },
      { name: "type", type: { dataClass: "ENTITY", refEntityId: "zen2.dataType" } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.dataType", embeddable: true, _system: true, fields: [
      { name: "dataClass", mandatory: true, type: { dataClass: "ENUM", enumerationId: "zen2.dataClasses" }},
      { name: "type", type: { dataClass: "ENUM", enumerationId: "zen2.scalarDataTypes" }},
      { name: "enumerationId", type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "refEntityId", type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "dataViewId", type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "mimeType", type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "lazy", type: { dataClass: "SCALAR", type: "BOOL" } },
      { name: "inverse", type: { dataClass: "SCALAR", type: "BOOL" } },
      { name: "backRef", type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "cascade", type: { dataClass: "SCALAR", type: "BOOL" } }
], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.enumeration", embeddable: false, _system: true, fields: [
      { name: "_id", pk: true, pkType: "ASSIGNED", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "_system", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" }, readOnly: true },
      { name: "constants", type: { dataClass: "LIST", type: "STRING" } }
], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.index", embeddable: true, _system: true, fields: [
      { name: "fields", type: { dataClass: "LIST", type: "STRING"} }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.scriptHandler", embeddable: false, _system: true, fields: [
      { name: "_id", pk: true, pkType: "AUTO", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "source", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "valid", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" }, readOnly: true },
      { name: "errors", type: { dataClass: "LIST", refEntityId: "zen2.scriptHandlerError" }, readOnly: true },
      { name: "consoleEntries", type: { dataClass: "LIST", refEntityId: "zen2.scriptHandlerConsoleEntry" }, readOnly: true }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.dataViewScriptHandler", parentEntityId: "zen2.scriptHandler", embeddable: false, _system: true, fields: [
    { name: "dataViewId", mandatory: true, type: { dataClass: "ENTITY", refEntityId: "zen2.dataView", lazy: true, dataViewId: "zen2.dataViewManage" } },                                                                                                                     
    { name: "triggerPoint", mandatory: true, type: { dataClass: "ENUM", enumerationId: "zen2.triggerPoints" } },
    { name: "triggerModes", mandatory: true, type: { dataClass: "LIST", enumerationId: "zen2.triggerModes" } }
], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.scriptHandlerError", embeddable: true, _system: true, fields: [
      { name: "date", mandatory: true, type: { dataClass: "SCALAR", type: "DATETIME" } }, 
      { name: "error", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "lineNo", mandatory: true, type: { dataClass: "SCALAR", type: "INT" } },
      { name: "colNo", mandatory: true, type: { dataClass: "SCALAR", type: "INT" } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.scriptHandlerConsoleEntry", embeddable: true, _system: true, fields: [
      { name: "date", mandatory: true, type: { dataClass: "SCALAR", type: "DATETIME" } }, 
      { name: "line", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.dataView", embeddable: false, _system: true, fields: [
       { name: "_id", pk: true, pkType: "ASSIGNED", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } },
       { name: "_system", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" }, readOnly: true },
       { name: "entity", mandatory: true, type: { dataClass: "ENTITY", refEntityId: "zen2.entity" } },
       { name: "overwriteMode", type: { dataClass: "ENUM", enumerationId: "zen2.overwriteModes" } },
       { name: "fields", type: { dataClass: "LIST", refEntityId: "zen2.fieldView" } },
       { name: "scope", type: { dataClass: "SCALAR", type: "STRING" } },
       { name: "pushScopes", type: { dataClass: "LIST", type: "STRING" } },
       { name: "allowedModes", type: { dataClass: "LIST", enumerationId: "zen2.commandModes" } },
       { name: "pushable", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" } },
       { name: "scriptHandlers", type: { dataClass: "LIST", refEntityId: "zen2.dataViewScriptHandler", lazy: true, cascade: true, inverse: true, backRef: "dataViewId", dataViewId: "zen2.dataViewScriptHandlerRead" } }
], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.fieldView", embeddable: true, _system: true, fields: [
     { name: "name", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } },
     { name: "dataViewId", type: { dataClass: "SCALAR", type: "STRING" } },
     { name: "mandatory", type: { dataClass: "SCALAR", type: "BOOL" } },
     { name: "readOnly", type: { dataClass: "SCALAR", type: "BOOL" } },
     { name: "lazy", type: { dataClass: "SCALAR", type: "BOOL" } },
     { name: "cascade", type: { dataClass: "SCALAR", type: "BOOL" } }
], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.resource", embeddable: false, _system: true, fields: [
    { name: "_id", pk: true, pkType: "ASSIGNED", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } },
    { name: "type", mandatory: true, type: { dataClass: "ENUM", enumerationId: "zen2.resourceTypes" } },
    { name: "customType", type: { dataClass: "SCALAR", type: "STRING" } },
    { name: "visibility", mandatory: true, type: { dataClass: "ENUM", enumerationId: "zen2.securityModes" } },
    { name: "description", type: { dataClass: "SCALAR", type: "STRING" } },
    { name: "preview", type: { dataClass: "SCALAR", type: "BINARY" } },
    { name: "size", type: { dataClass: "SCALAR", type: "INT" }, readOnly: true },
    { name: "content", type: { dataClass: "BINARY" } },
    { name: "textContent", type: { dataClass: "SCALAR", type: "STRING" } }
], _class: "de.zeos.zen2.app.model.Entity" } );

db.zen2.entity.insert( { _id: "zen2.template", embeddable: false, _system: true, fields: [
    { name: "_id", pk: true, pkType: "ASSIGNED", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } },
    { name: "type", mandatory: true, type: { dataClass: "ENUM", enumerationId: "zen2.templateTypes" } },
    { name: "description", type: { dataClass: "SCALAR", type: "STRING" } },
    { name: "content", type: { dataClass: "SCALAR", type: "STRING" } }
], _class: "de.zeos.zen2.app.model.Entity" } );


db.zen2.dataView.insert( { _id: "zen2.app", _system: true, entity: { $ref: "zen2.entity", $id: "zen2.application"}, overwriteMode: "WHITELIST", fields: [{name: "_id"}], allowedModes: ["READ"], _class: "de.zeos.zen2.app.model.DataView" } );
db.zen2.dataView.insert( { _id: "zen2.appManage", _system: true, entity: { $ref: "zen2.entity", $id: "zen2.application"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );
db.zen2.dataView.insert( { _id: "zen2.scriptHandlerRead", _system: true, scope: "application", entity: { $ref: "zen2.entity", $id: "zen2.scriptHandler"}, allowedModes: ["READ"], _class: "de.zeos.zen2.app.model.DataView" } );
db.zen2.dataView.insert( { _id: "zen2.scriptHandlerSourceUpdate", _system: true, scope: "application", entity: { $ref: "zen2.entity", $id: "zen2.scriptHandler"}, overwriteMode: "WHITELIST", fields: [{name: "_id"}, {name: "source"}], allowedModes: ["UPDATE"], _class: "de.zeos.zen2.app.model.DataView" } );
db.zen2.dataView.insert( { _id: "zen2.scriptHandlerConsoleUpdate", _system: true, scope: "application", entity: { $ref: "zen2.entity", $id: "zen2.scriptHandler"}, overwriteMode: "WHITELIST", fields: [{name: "_id"}, {name: "consoleEntries", readOnly: false}], allowedModes: ["UPDATE"], _class: "de.zeos.zen2.app.model.DataView" } );
db.zen2.dataView.insert( { _id: "zen2.scriptHandlerErrorUpdate", _system: true, scope: "application", entity: { $ref: "zen2.entity", $id: "zen2.scriptHandler"}, overwriteMode: "WHITELIST", fields: [{name: "_id"}, {name: "errors", readOnly: false}], allowedModes: ["UPDATE"], _class: "de.zeos.zen2.app.model.DataView" } );
db.zen2.dataView.insert( { _id: "zen2.dataViewScriptHandlerRead", _system: true, scope: "application", entity: { $ref: "zen2.entity", $id: "zen2.dataViewScriptHandler"}, allowedModes: ["READ"], _class: "de.zeos.zen2.app.model.DataView" } );
db.zen2.dataView.insert( { _id: "zen2.entityManage", _system: true, scope: "application", entity: { $ref: "zen2.entity", $id: "zen2.entity"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );
db.zen2.dataView.insert( { _id: "zen2.enumerationManage", _system: true, scope: "application", entity: { $ref: "zen2.entity", $id: "zen2.enumeration"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );
db.zen2.dataView.insert( { _id: "zen2.dataViewManage", _system: true, scope: "application", entity: { $ref: "zen2.entity", $id: "zen2.dataView"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );
db.zen2.dataView.insert( { _id: "zen2.resourceManage", _system: true, scope: "application", entity: { $ref: "zen2.entity", $id: "zen2.resource"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );
db.zen2.dataView.insert( { _id: "zen2.templateManage", _system: true, scope: "application", entity: { $ref: "zen2.entity", $id: "zen2.template"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );