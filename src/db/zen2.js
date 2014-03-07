db.application.insert( { _id: "zen2", system: true, securityMode: "PUBLIC", securityHandler: null, _class: "de.zeos.zen2.app.model.Application" } );

db.enumeration.insert( { _id: "templateTypes", system: true, constants: [ "HTML", "JS" ], _class: "de.zeos.zen2.app.model.Enumeration" } );

db.entity.insert( { _id: "template", embeddable: false, system: true, fields: [
   { name: "_id", pk: true, pkType: "ASSIGNED", mandatory: true, type: { dataClass: "SCALAR", type: "STRING" } },
   { name: "type", mandatory: true, type: { dataClass: "ENUM", enumerationId: "templateTypes" } },
   { name: "description", type: { dataClass: "SCALAR", type: "STRING" } },
   { name: "content", type: { dataClass: "SCALAR", type: "STRING" } }
], _class: "de.zeos.zen2.app.model.Entity" } );

db.dataView.insert( { _id: "templateManage", system: true, scope: "application", entity: { $ref: "entity", $id: "template"}, allowedModes: ["CREATE", "READ", "UPDATE", "DELETE"], _class: "de.zeos.zen2.app.model.DataView" } );