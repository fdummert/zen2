db.application.insert( { _id: "zen2", securityMode: "PUBLIC", securityHandler: null, _class: "de.zeos.zen2.app.model.Application" } );
db.enumeration.insert( { _id: "securityModes", constants: [ "PUBLIC", "PROTECTED" ], _class: "de.zeos.zen2.app.model.Enumeration" } );

db.entity.insert( { _id: "application", embeddable: false, fields: [
      { name: "_id", pk: true, mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "securityMode", type: { dataClass: "ENUM", enumeration: { $ref: "enumeration", $id: "securityModes"} }}, 
      { name: "securityHandler", type: { dataClass: "ENTITY", refEntity: { $ref: "entity", $id: "scriptHandler" } } }
 ], _class: "de.zeos.zen2.app.model.Entity" } );

db.entity.insert( { _id: "scriptHandler", embeddable: false, fields: [
      { name: "_id", pk: true, mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "source", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } }, 
      { name: "valid", mandatory: true, type: { dataClass: "SCALAR", type: "BOOL" }, readOnly: true },
      { name: "errors", type: { dataClass: "LIST", refEntity: { $ref: "entity", $id: "scriptHandlerError"} } },
      { name: "consoleEntries", type: { dataClass: "LIST", refEntity: { $ref: "entity", $id: "scriptHandlerConsoleEntry"} } }
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

db.dataView.insert( { _id: "appView", entity: { $ref: "entity", $id: "application"}, fields: [{name: "_id"}], allowedModes: ["READ"], _class: "de.zeos.zen2.app.model.DataView" } );
db.dataView.insert( { _id: "appManageView", entity: { $ref: "entity", $id: "application"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );