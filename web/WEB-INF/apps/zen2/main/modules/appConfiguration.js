define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.DynamicForm.create({
                    ID: "appConfigForm",
                    dataSource: appManageDS,
                    fields: [
                        { name: "_id", canEdit: app == null },
                        { name: "securityMode", redrawOnChange: true, defaultToFirstOption: true },
                        { name: "securityHandlerButton", showIf: "form.getValue('securityMode') == 'PROTECTED'", type: "canvas", title: msgs.securityHandler, canvasConstructor: "Button",
                            canvasProperties: { title: "...", 
                                click: function() {
                                    require(["./scriptHandler"], function(handler) {
                                        var def = "function authenticate(credentials, db, digester) {\n    $\n}";
                                        handler.show(cm, msgs.securityHandler, appConfigForm.getValue("securityHandler"), def, function(updatedHandler) {
                                            updatedHandler.valid = true;
                                            var secHandler = appConfigForm.getValue("securityHandler");
                                            if (secHandler == null)
                                                appConfigForm.setValue("securityHandler", updatedHandler);
                                            appConfigForm.getField("securityHandlerButton").canvas.setBorder("1px solid orange");
                                        }, function(savedHandler) {
                                            appConfigForm.getField("securityHandlerButton").canvas.setBorder("1px solid green");
                                            savedHandler.valid = true;
                                        });
                                    });
                                } 
                        }}
                    ]
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.save,
                            click: function() { 
                                appConfigForm.saveData(function(res, data, req) {
                                    if (res.status == isc.DSResponse.STATUS_SUCCESS && data.securityHandler != null)
                                        appConfigForm.getField("securityHandlerButton").canvas.setBorder("1px solid green");
                                    if (req.operationType == "add") {
                                        applications.invalidateCache();
                                    }
                                }); 
                            }
                        })
                    ]
                })
            ];
            if (app == null)
                appConfigForm.editNewRecord();
            else {
                appManageDS.fetchData({_id: app._id}, function(res, data) {
                    appConfigForm.editRecord(data[0]);
                    if (data[0].securityHandler != null) {
                        appConfigForm.getField("securityHandlerButton").canvas.setBorder("1px solid green");
                    }
                });
            }
            return list;
        }
    };
});