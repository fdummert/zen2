define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.ListGrid.create({
                    dataSource: dataViewManageDS,
                    autoFetchData: true,
                    canRemoveRecords: true,
                    warnOnRemoval: true,
                    warnOnRemovalMessage: msgs.warnRemove,
                    showResizeBar: true,
                    recordClick: function(viewer, rec) {
                        dataViewScriptHandlerReadDS.fetchData({dataViewId: rec._id}, function(res, data) {
                            console.log("fetch data", data);
                            rec.scriptHandlers = data;
                            dataViewManageForm.editRecord(rec);
                        });
                    }
                }),
                isc.DynamicForm.create({
                    ID: "dataViewManageForm",
                    dataSource: dataViewManageDS,
                    useAllDataSourceFields: true,
                    fields: [
                        { name: "scriptHandlers", title: msgs.dataViewHandler, editorType: "GridEditorItem", cm: cm, msgs: msgs, gridDataSource: "dataViewScriptHandlerReadDS", 
                            gridFields: [
                                 {name: "triggerPoint"}, {name: "triggerModes"}, {name: "valid"}, 
                                 {name: "editField", type: "icon", title: msgs.edit, canEdit: false, cellIcon: "[SKIN]actions/edit.png", 
                                     recordClick: function(viewer, record, recordNum, field, fieldNum) {
                                         var oldRecord = isc.addProperties({}, record);
                                         function scriptHandlerLoaded(handler, scriptHandler) {
                                             var def = "function process(data, scope, " + (scriptHandler.triggerPoint == "AFTER" ? "result, " : "") + "db) {\n    $\n}";
                                             handler.show(viewer.msgs.dataViewHandler + ": " + scriptHandler.triggerPoint + " " + scriptHandler.triggerModes, scriptHandler, def, function(updatedHandler) {
                                                 record._changed = !isc.DynamicForm.compareValues(oldRecord, updatedHandler);
                                                 updatedHandler.valid = true;
                                                 viewer.refreshCell(recordNum, viewer.getFieldNum("valid"));
                                                 console.log("applied", updatedHandler);
                                             }, function(savedHandler) {
                                                 console.log("saved locally", savedHandler);
                                             });
                                         }
                                         require(["./scriptHandler"], function(handler) {
                                             scriptHandlerLoaded(handler, record);
                                         });
                                     }
                                 }
                            ],
                            gridProperties: {
                                cm: cm,
                                msgs: msgs,
                                canRemoveRecords: true,
                                initialSort: [{
                                    direction: "ascending",
                                    property: "triggerModes"
                                }, {
                                    direction: "ascending",
                                    property: "triggerPoint",
                                    normalizer: function(item) {
                                        return {
                                            "BEFORE_PROCESSING": 1,
                                            "BEFORE": 2,
                                            "AFTER": 3
                                        }[item];
                                    }
                                }],
                                getCellCSSText: function (record, rowNum, colNum) {
                                    var name = this.getFieldName(colNum);
                                    var style = "";
                                    if (name == "editField") style += "cursor: pointer;";
                                    if (record && record._changed === true) style += "background-color: orange;";
                                    if (style.length > 0)
                                        return style;
                                }
                            }
                        }
                    ]
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { dataViewManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.DataView"}); }
                        }),
                        isc.Button.create({
                            title: msgs.save,
                            click: function() { 
                                dataViewManageForm.saveData(function(res, data) {
                                    dataViewScriptHandlerReadDS.fetchData({dataViewId: data._id}, function(fres, fdata) {
                                        data.scriptHandlers = fdata;
                                        dataViewManageForm.editRecord(data);
                                    });
                                }); 
                            }
                        })
                    ]
                })
            ];
            dataViewManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.DataView"});
            return list;
        }
    };
});