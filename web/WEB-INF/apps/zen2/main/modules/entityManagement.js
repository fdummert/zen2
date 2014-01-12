define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.ListGrid.create({
                    dataSource: entityManageViewDS,
                    autoFetchData: true,
                    canRemoveRecords: true,
                    warnOnRemoval: true,
                    warnOnRemovalMessage: msgs.warnRemove,
                    showResizeBar: true,
                    recordClick: function(viewer, rec) {
                        entityManageForm.editRecord(rec);
                    }
                }),
                isc.DynamicForm.create({
                    ID: "entityManageForm",
                    dataSource: entityManageViewDS,
                    useAllDataSourceFields: true
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { entityManageForm.editNewRecord(); }
                        }),
                        isc.Button.create({
                            title: msgs.save,
                            click: function() { entityManageForm.saveData(); }
                        })
                    ]
                })
            ];
            entityManageForm.editNewRecord();
            return list;
        }
    };
});