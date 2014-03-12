define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.ListGrid.create({
                    dataSource: zen2_enumerationManageDS,
                    autoFetchData: true,
                    canRemoveRecords: true,
                    warnOnRemoval: true,
                    warnOnRemovalMessage: msgs.warnRemove,
                    showResizeBar: true,
                    recordClick: function(viewer, rec) {
                        if (rec._canEdit === false)
                            enumerationManageSaveButton.setDisabled(true);
                        enumerationManageForm.editRecord(rec);
                    }
                }),
                isc.DynamicForm.create({
                    ID: "enumerationManageForm",
                    dataSource: zen2_enumerationManageDS,
                    useAllDataSourceFields: true
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { enumerationManageSaveButton.setDisabled(false); enumerationManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Enumeration"}); }
                        }),
                        isc.Button.create({
                            ID: "enumerationManageSaveButton",
                            title: msgs.save,
                            click: function() { enumerationManageForm.saveData(); }
                        })
                    ]
                })
            ];
            enumerationManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Enumeration"});
            return list;
        }
    };
});