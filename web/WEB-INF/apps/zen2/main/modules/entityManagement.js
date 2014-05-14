define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.ListGrid.create({
                    dataSource: zen2_entityManageDS,
                    autoFetchData: true,
                    canRemoveRecords: true,
                    warnOnRemoval: true,
                    warnOnRemovalMessage: msgs.warnRemove,
                    showResizeBar: true,
                    fields: [
                         { name: "_id" },
                         { name: "embeddable" },
                         { name: "_system" }
                    ],
                    recordClick: function(viewer, rec) {
                        entityManageSaveButton.setDisabled(rec._canEdit === false);
                        entityManageForm.editRecord(rec);
                    }
                }),
                isc.DynamicForm.create({
                    ID: "entityManageForm",
                    dataSource: zen2_entityManageDS,
                    useAllDataSourceFields: true,
                    showComplexFieldsRecursively: true
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { entityManageSaveButton.setDisabled(false); entityManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Entity"}); }
                        }),
                        isc.Button.create({
                            ID: "entityManageSaveButton",
                            title: msgs.save,
                            click: function() { entityManageForm.saveData(); }
                        })
                    ]
                })
            ];
            entityManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Entity"});
            return list;
        }
    };
});