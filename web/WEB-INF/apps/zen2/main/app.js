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
                        click: function() { 
                            openModule("appConfiguration", applications.getSelectedRecord());
                        } 
                    }, 
                    { title: msgs.add,
                        click: function() {
                            openModule("appConfiguration");
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
                                dataSource: appViewDS,
                                autoFetchData: true,
                                contextMenu: appMenu,
                                recordClick: function(viewer, record) {
                                    content.addMember(isc.Label.create({contents: "click", height: 10}));
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