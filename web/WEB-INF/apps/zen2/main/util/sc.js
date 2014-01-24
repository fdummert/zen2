define(["require"], function(require) {
    isc.ClassFactory.defineClass("HandlerEditorItem", "CanvasItem");
    isc.HandlerEditorItem.addProperties({
        cm: null,
        msgs: null,
        canvasConstructor: "Button", 
        shouldSaveValue: true,
        canvasProperties: { title: "...",
            click: function() {
                var that = this.canvasItem;
                function scriptHandlerLoaded(handler, scriptHandler) {
                    handler.show(that.cm, that.msgs.beforeHandler, scriptHandler, function(updatedHandler) {
                        updatedHandler.valid = true;
                        if (scriptHandler == null)
                            that.setValue(updatedHandler);
                        that.pendingValue();
                    }, function(savedHandler) {
                        if (savedHandler != null)
                            that.setValue(savedHandler);
                    });
                }
                require(["../modules/scriptHandler"], function(handler) {
                    var currentHandler = that.getValue();
                    if (currentHandler != null && typeof currentHandler === "string") {
                        scriptHandlerReadDS.fetchData({_id: currentHandler}, function(res, data) {
                            currentHandler = data[0];
                            that.setValue(currentHandler);
                            scriptHandlerLoaded(handler, currentHandler);
                        });
                    } else {
                        scriptHandlerLoaded(handler, currentHandler);
                    }
                });
            }
        },
        icons: [{
            src: "[SKIN]actions/remove.png", disabled: true,
            click: function(form, item) {
                item.setValue(null);
                item.pendingValue();
            }
        }], 
        iconIsDisabled : function (icon) {
            icon = this.getIcon(icon);
            if (icon.disabled)
                return true;
            return this.Super("iconIsDisabled", arguments);
        }, 
        pendingValue: function() {
            var old = this.form.getOldValue(this.name);
            var current = this.getValue();
            var unchanged = old == current || (old != null && current != null && old.source == current.source);
            this.canvas.setBorder(unchanged ? (current == null ? null : "1px solid green") : "1px solid orange");
            this.updateIcon(current);
        },
        updateIcon: function(value) {
            this.getIcon(0).disabled = value == null;
            this.setIconEnabled(0);
        },
        unchangedValue: function() {
            var value = this.getValue();
            this.canvas.setBorder(value == null ? null : "1px solid green");
            this.updateIcon(value);
        }
    });
});

