define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.ListGrid.create({
                    dataSource: templateManageDS,
                    autoFetchData: true,
                    canRemoveRecords: true,
                    warnOnRemoval: true,
                    warnOnRemovalMessage: msgs.warnRemove,
                    showResizeBar: true,
                    recordClick: function(viewer, rec) {
                        templateManageForm.editRecord(rec);
                    },
                    fields: [
                        { name: "_id" },
                        { name: "type" },
                        { name: "description" }
                    ]
                }),
                isc.DynamicForm.create({
                    ID: "templateManageForm",
                    dataSource: templateManageDS,
                    useAllDataSourceFields: true,
                    fields: [
                        { name: "type", defaultToFirstOption: true },
                        { name: "content", type: "canvas", shouldSaveValue: true, canvasConstructor: "Button", 
                            canvasProperties: { title: "...", 
                                click: function() {
                                    require(["./textEditor"], function(editor) {
                                        var types = {
                                            "HTML": "text/html",
                                            "JS": "text/javascript"
                                        };
                                        var mimeType = types[templateManageForm.getValue("type")];
                                        editor.show(templateManageForm.getValue("_id"), templateManageForm.getValue("type"), mimeType, templateManageForm.getValue("content"), false, function(updatedValue) {
                                            templateManageForm.setValue("content", updatedValue);
                                            templateManageForm.getField("content").canvas.setBorder("1px solid orange");
                                        });
                                    });
                                }
                            } 
                        }
                    ]
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { 
                                templateManageForm.getField("content").canvas.setBorder(null);
                                templateManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Resource"});
                            }
                        }),
                        isc.Button.create({
                            title: msgs.save,
                            click: function() { 
                                templateManageForm.saveData(function(res, data, req) {
                                    if (res.status == isc.DSResponse.STATUS_SUCCESS) {
                                        templateManageForm.getField("content").canvas.setBorder("1px solid green");
                                    }
                                });
                            }
                        })
                    ]
                })
            ];
            templateManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Resource"});
            return list;
        }
    };
});