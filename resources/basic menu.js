define(["js/dojo/i18n!${1:app}/nls/messages"], function(msgs) {
    return {
        start: function(cm) {
            var currentModule = null;
            function openModule(name) {
                if (currentModule && currentModule.onDestroy) {
                    var ret = currentModule.onDestroy();
                    if (ret === false)
                        return;
                }
                for (var i = 0; i < content.members.length; i++) {
                    var member = content.members[i];
                    member.destroy();
                }
                require(["./" + name + ".js?sessionId=" + cm.getClientId()], function(mod) {
                    currentModule = mod;
                    content.setMembers(mod.create(cm));
                });
            }
            
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