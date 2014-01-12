db.application.insert( { _id: "zen2", securityMode: "PUBLIC", securityHandler: null, _class: "de.zeos.zen2.app.model.Application" } );
db.enumeration.insert( { _id: "securityModes", constants: [ "PUBLIC", "PROTECTED" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.enumeration.insert( { _id: "dataClasses", constants: [ "SCALAR", "ENUM", "LIST", "ENTITY" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.enumeration.insert( { _id: "scalarDataTypes", constants: [ "BOOL", "INT", "FLOAT", "DATE", "DATETIME", "TIME", "PASSWORD", "STRING" ], _class: "de.zeos.zen2.app.model.Enumeration" } );
db.enumeration.insert( { _id: "commandModes", constants: [ "CREATE", "READ", "UPDATE", "DELETE" ], _class: "de.zeos.zen2.app.model.Enumeration" } );

db.entity.insert( { _id: "application", embeddable: false, fields: [
      { name: "_id", pk: true, mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "securityMode", type: { dataClass: "ENUM", enumerationId: "securityModes" }}, 
      { name: "securityHandler", type: { dataClass: "ENTITY", refEntityId: "scriptHandler", lazy: false, cascade: true } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.entity.insert( { _id: "entity", embeddable: false, fields: [
      { name: "_id", pk: true, mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "embeddable", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" } },
      { name: "fields", type: { dataClass: "LIST", refEntityId: "field" } },
      { name: "indexes", type: { dataClass: "LIST", refEntityId: "index" } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.entity.insert( { _id: "field", embeddable: true, fields: [
      { name: "name", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "pk", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" } },
      { name: "mandatory", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" } },
      { name: "readOnly", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" } },
      { name: "type", type: { dataClass: "ENTITY", refEntityId: "dataType" } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.entity.insert( { _id: "dataType", embeddable: true, fields: [
      { name: "dataClass", mandatory: true, type: { dataClass: "ENUM", enumerationId: "dataClasses" }},
      { name: "type", type: { dataClass: "ENUM", enumerationId: "scalarDataTypes" }},
      { name: "enumerationId", type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "refEntityId", type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "lazy", type: { dataClass: "SCALAR", type: "BOOL" } },
      { name: "cascade", type: { dataClass: "SCALAR", type: "BOOL" } }
], _class: "de.zeos.zen2.app.model.Entity" } );

db.entity.insert( { _id: "enumeration", embeddable: false, fields: [
      { name: "_id", pk: true, mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "constants", type: { dataClass: "LIST", type: "STRING" } }
], _class: "de.zeos.zen2.app.model.Entity" } );

db.entity.insert( { _id: "index", embeddable: true, fields: [
      { name: "fields", type: { dataClass: "LIST", type: "STRING"} }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.entity.insert( { _id: "scriptHandler", embeddable: false, fields: [
      { name: "_id", pk: true, mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "source", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "valid", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" }, readOnly: true },
      { name: "errors", type: { dataClass: "LIST", refEntityId: "scriptHandlerError" } },
      { name: "consoleEntries", type: { dataClass: "LIST", refEntityId: "scriptHandlerConsoleEntry" } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.entity.insert( { _id: "scriptHandlerError", embeddable: true, fields: [
      { name: "date", mandatory: true, type: { dataClass: "SCALAR", type: "DATETIME" } }, 
      { name: "error", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } },
      { name: "lineNo", mandatory: true, type: { dataClass: "SCALAR", type: "INT" } },
      { name: "colNo", mandatory: true, type: { dataClass: "SCALAR", type: "INT" } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.entity.insert( { _id: "scriptHandlerConsoleEntry", embeddable: true, fields: [
      { name: "date", mandatory: true, type: { dataClass: "SCALAR", type: "DATETIME" } }, 
      { name: "line", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.entity.insert( { _id: "dataView", embeddable: false, fields: [
       { name: "_id", pk: true, mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
       { name: "entity", mandatory: true, type: { dataClass: "ENTITY", refEntityId: "entity" } },
       { name: "fields", type: { dataClass: "LIST", refEntityId: "fieldView" } },
       { name: "scope", type: { dataClass: "SCALAR", type: "STRING" } },
       { name: "pushScopes", type: { dataClass: "LIST", type: "STRING" } },
       { name: "allowedModes", type: { dataClass: "LIST", enumerationId: "commandModes" } },
       { name: "pushable", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" } }
], _class: "de.zeos.zen2.app.model.Entity" } );

db.entity.insert( { _id: "fieldView", embeddable: true, fields: [
     { name: "name", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } },
     { name: "mandatory", type: { dataClass: "SCALAR", type: "BOOL" } },
     { name: "readOnly", type: { dataClass: "SCALAR", type: "BOOL" } },
     { name: "lazy", type: { dataClass: "SCALAR", type: "BOOL" } },
     { name: "cascade", type: { dataClass: "SCALAR", type: "BOOL" } }
], _class: "de.zeos.zen2.app.model.Entity" } );

db.dataView.insert( { _id: "appView", entity: { $ref: "entity", $id: "application"}, fields: [{name: "_id"}], allowedModes: ["READ"], _class: "de.zeos.zen2.app.model.DataView" } );
db.dataView.insert( { _id: "appManageView", entity: { $ref: "entity", $id: "application"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );

db.dataView.insert( { _id: "entityManageView", entity: { $ref: "entity", $id: "entity"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );
db.dataView.insert( { _id: "enumerationManageView", entity: { $ref: "entity", $id: "enumeration"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );
db.dataView.insert( { _id: "dataViewManageView", entity: { $ref: "entity", $id: "dataView"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );