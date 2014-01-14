define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.ListGrid.create({
                    dataSource: enumerationManageViewDS,
                    autoFetchData: true,
                    canRemoveRecords: true,
                    warnOnRemoval: true,
                    warnOnRemovalMessage: msgs.warnRemove,
                    showResizeBar: true,
                    recordClick: function(viewer, rec) {
                        enumerationManageForm.editRecord(rec);
                    }
                }),
                isc.DynamicForm.create({
                    ID: "enumerationManageForm",
                    dataSource: enumerationManageViewDS,
                    useAllDataSourceFields: true
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { enumerationManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Enumeration"}); }
                        }),
                        isc.Button.create({
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