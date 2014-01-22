define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.DynamicForm.create({
                    ID: "appConfigForm",
                    dataSource: appManageDS,
                    fields: [
                        { name: "_id", canEdit: app == null },
                        { name: "securityMode", redrawOnChange: true },
                        { name: "securityHandlerButton", showIf: "form.getValue('securityMode') == 'PROTECTED'", type: "canvas", title: msgs.securityHandler, canvasConstructor: "Button",
                            canvasProperties: { title: "...", 
                                click: function() {
                                    require(["./scriptHandler"], function(handler) {
                                        handler.show(cm, msgs.securityHandler, appConfigForm.getValue("securityHandler"), function(updatedHandler) {
                                            appConfigForm.setValue("securityHandler", updatedHandler);
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
                            click: function() { appConfigForm.saveData(); }
                        })
                    ]
                })
            ];
            if (app == null)
                appConfigForm.editNewRecord();
            else {
                appManageDS.fetchData({_id: app._id}, function(res, data) {
                    appConfigForm.editRecord(data[0]);
                });
            }
            return list;
        }
    };
});