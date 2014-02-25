define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.ListGrid.create({
                    dataSource: resourceManageDS,
                    autoFetchData: true,
                    canRemoveRecords: true,
                    warnOnRemoval: true,
                    warnOnRemovalMessage: msgs.warnRemove,
                    showResizeBar: true,
                    recordClick: function(viewer, rec) {
                        resourceManageForm.editRecord(rec);
                    }
                }),
                isc.DynamicForm.create({
                    ID: "resourceManageForm",
                    dataSource: resourceManageDS,
                    useAllDataSourceFields: true
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { resourceManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Resource"}); }
                        }),
                        isc.Button.create({
                            title: msgs.save,
                            click: function() { resourceManageForm.saveData(); }
                        })
                    ]
                })
            ];
            resourceManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Resource"});
            return list;
        }
    };
});