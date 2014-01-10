define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var currentModule = null;
            function openModule(name, context) {
                if (currentModule && currentModule.onDestroy) {
                    var ret = currentModule.onDestroy();
                    if (ret === false)
                        return;
                }
                for (var i = 0; i < subContent.members.length; i++) {
                    var member = subContent.members[i];
                    member.destroy();
                }
                require(["./" + name], function(mod) {
                    currentModule = mod;
                    subContent.setMembers(mod.create(cm, context));
                });
            }
            var list = [
                isc.HLayout.create({
                    height: 50,
                    members: [
                        isc.Button.create({
                            title: msgs.entities,
                            click: function() { openModule("entityManagement", app); }
                        }),
                        isc.Button.create({
                            title: msgs.enumerations,
                            click: function() { openModule("enumerationManagement", app); }
                        }),
                        isc.Button.create({
                            title: msgs.dataViews,
                            click: function() { openModule("dataViewManagement", app); }
                        })
                    ]
                }),
                isc.VLayout.create({
                    ID: "subContent",
                    height: "*",
                    layoutMargin: 10,   
                    members: [
                        
                    ]
                })
            ];
            return list;
        }
    };
});