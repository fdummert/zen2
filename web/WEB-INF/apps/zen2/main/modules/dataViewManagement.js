define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.Label.create({contents: "DataView Management"})
            ];
            return list;
        }
    };
});