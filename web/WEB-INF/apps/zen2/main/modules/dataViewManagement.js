define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.ListGrid.create({
                    dataSource: dataViewManageDS,
                    autoFetchData: true,
                    canRemoveRecords: true,
                    warnOnRemoval: true,
                    warnOnRemovalMessage: msgs.warnRemove,
                    showResizeBar: true,
                    recordClick: function(viewer, rec) {
                        dataViewManageForm.editRecord(rec);
                    }
                }),
                isc.DynamicForm.create({
                    ID: "dataViewManageForm",
                    dataSource: dataViewManageDS,
                    useAllDataSourceFields: true,
                    fields: [
                        { name: "beforeHandler", type: "canvas", title: msgs.beforeHandler, canvasConstructor: "Button",
                            canvasProperties: { title: "...",
                                click: function() {
                                    var that = this.canvasItem;
                                    require(["./scriptHandler"], function(handler) {
                                        scriptHandlerManageDS.fetchData({_id: that.getValue()}, function(res, data) {
                                            handler.show(cm, msgs.beforeHandler, data[0], null, function(savedHandler) {
                                                if (savedHandler != null)
                                                    that.setValue(savedHandler._id);
                                            });
                                        });
                                    });
                                }
                            }, icons: [{
                                src: "[SKIN]actions/remove.png",
                                click: function() {
                                    var that = this;
                                    isc.confirm(msgs.warnRemove, function(value) {
                                        if (value === true) {
                                            scriptHandlerManageDS.removeData(that.getValue(), function(res, data) {
                                                that.setValue(null);
                                            });
                                        }
                                    })
                                }
                            }], iconIsDisabled : function (icon) {
                                icon = this.getIcon(icon);
                                if (icon.disabled)
                                    return true;
                                return this.Super("iconIsDisabled", arguments);
                            }, showIf: function(item, value, form, values) {
                                if (value != null) {
                                    this.canvas.setBorder("1px solid green");
                                }
                                this.getIcon(0).disabled = value == null;
                                this.setIconEnabled(0);
                                return values._id != null;
                            }
                        }
                    ]
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { dataViewManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.DataView"}); }
                        }),
                        isc.Button.create({
                            title: msgs.save,
                            click: function() { dataViewManageForm.saveData(); }
                        })
                    ]
                })
            ];
            dataViewManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.DataView"});
            return list;
        }
    };
});