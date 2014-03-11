define(["js/dojo/i18n!${1:app}/nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.ListGrid.create({
                    dataSource: ${2:dataSource}DS,
                    autoFetchData: true,
                    canRemoveRecords: true,
                    warnOnRemoval: true,
                    warnOnRemovalMessage: msgs.warnRemove,
                    showResizeBar: true,
                    recordClick: function(viewer, rec) {
                        ${2}Form.editRecord(rec);
                    }
                }),
                isc.DynamicForm.create({
                    ID: "${2}Form",
                    dataSource: ${2}DS,
                    useAllDataSourceFields: true
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { ${2}Form.editNewRecord(); }
                        }),
                        isc.Button.create({
                            title: msgs.save,
                            click: function() { ${2}Form.saveData(); }
                        })
                    ]
                })
            ];
            ${2}Form.editNewRecord();
            return list;
        }
    };
});