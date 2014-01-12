define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.ListGrid.create({
                    dataSource: dataViewManageViewDS,
                    autoFetchData: true,
                    canRemoveRecords: true,
                    warnOnRemoval: true,
                    warnOnRemovalMessage: msgs.warnRemove,
                    showResizeBar: true,
                    recordClick: function(viewer, rec) {
                        dataViewManageForm.editRecord(rec);
                    }
                }),
                isc.DynamicForm.create({
                    ID: "dataViewManageForm",
                    dataSource: dataViewManageViewDS,
                    useAllDataSourceFields: true
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { dataViewManageForm.editNewRecord(); }
                        }),
                        isc.Button.create({
                            title: msgs.save,
                            click: function() { dataViewManageForm.saveData(); }
                        })
                    ]
                })
            ];
            dataViewManageForm.editNewRecord();
            return list;
        }
    };
});