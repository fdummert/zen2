define(["dojo/i18n!../nls/messages", "de/zeos/cometd/sc/cometdDataSource", "require"], function(msgs, unused, require) {
    return {
        start: function(cm) {
            var currentModule = null;
            function openModule(name, context) {
                if (currentModule && currentModule.onDestroy) {
                    var ret = currentModule.onDestroy();
                    if (ret === false)
                        return;
                }
                for (var i = 0; i < content.members.length; i++) {
                    var member = content.members[i];
                    member.destroy();
                }
                require(["./modules/" + name], function(mod) {
                    currentModule = mod;
                    content.setMembers(mod.create(cm, context));
                });
            }
            
            isc.Menu.create({
                ID: "appMenu",
                data: [
                    { title: msgs.configure,
                        enableIf: function() {
                            return applications.getSelectedRecord() != null;
                        },
                        click: function() { 
                            openModule("appConfiguration", applications.getSelectedRecord());
                        } 
                    }, 
                    { title: msgs.add,
                        click: function() {
                            openModule("appConfiguration");
                        }
                    },
                    { title: msgs.remove, 
                        enableIf: function() {
                            return applications.getSelectedRecord() != null;
                        },
                        click: function() { 
                            isc.confirm(msgs.warnRemoveApp, function(value) {
                                if (value === true) {
                                    var rec = applications.getSelectedRecord();
                                    appManageDS.removeData(rec, function() {
                                        applications.invalidateCache();
                                    });
                                }
                            });
                        } 
                    }
                ]
            });
            return isc.HLayout.create({
                width: "100%",
                height: "100%",
                autoDraw: true,
                members: [
                    isc.VLayout.create({
                        ID: "navigation",
                        width: 300,
                        showResizeBar: true,
                        membersMargin: 10,
                        layoutMargin: 10,
                        members: [
                            isc.Label.create({contents: msgs.welcome}),
                            isc.ListGrid.create({
                                ID: "applications",
                                dataSource: appDS,
                                autoFetchData: true,
                                contextMenu: appMenu,
                                recordClick: function(viewer, record) {
                                    openModule("appManagement", record);
                                }
                            }),
                            isc.Button.create({title: msgs.logout, width: 250, height: 20, click: function() { cm.stop(); }})
                        ]
                    }),
                    isc.VLayout.create({
                        ID: "content",
                        width: "*",
                        layoutMargin: 10,   
                        members: [
                            
                        ]
                    })
                ]
            });
        }
    };
});