define(["dojo/i18n!../nls/messages", "require"], function(msgs, require) {
    return {
        start: function(cm) {
            isc.Label.create({contents: msgs.welcome}).show();
        }
    };
});