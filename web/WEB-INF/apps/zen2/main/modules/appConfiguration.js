define(["dojo/i18n!../../nls/messages"], function(msgs) {
    return {
        create: function(cm, app) {
            var list = [
                isc.DynamicForm.create({
                    ID: "appConfigForm",
                    dataSource: appManageViewDS,
                    useAllDataSourceFields: true,
                    fields: [
                        { name: "_id", canEdit: app == null },
                        { name: "securityMode", redrawOnChange: true }
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
                appManageViewDS.fetchData({_id: app._id}, function(res, data) {
                    appConfigForm.editRecord(data[0]);
                });
            }
            return list;
        }
    };
});