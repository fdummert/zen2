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
                        dataViewManageForm.editRecord(rec);
                        dataViewManageForm.unchanged();
                    }
                }),
                isc.DynamicForm.create({
                    ID: "dataViewManageForm",
                    dataSource: dataViewManageDS,
                    useAllDataSourceFields: true,
                    fields: [
                        { name: "beforeHandler", type: "HandlerEditorItem", title: msgs.beforeHandler, cm: cm, msgs: msgs}
                    ],
                    unchanged: function() {
                        for (var i = 0; i < this.getFields().length; i++) {
                            var f = this.getField(i);
                            if (f.unchangedValue != null) {
                                f.unchangedValue();
                            }
                        }
                    }
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
                                    if (res.status == isc.DSResponse.STATUS_SUCCESS)
                                        dataViewManageForm.unchanged();
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