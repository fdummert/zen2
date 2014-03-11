define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var list = [
                isc.ListGrid.create({
                    dataSource: resourceManageDS,
                    autoFetchData: true,
                    canRemoveRecords: true,
                    warnOnRemoval: true,
                    warnOnRemovalMessage: msgs.warnRemove,
                    showResizeBar: true,
                    recordClick: function(viewer, rec) {
                        resourceManageForm.editRecord(rec);
                    },
                    fields: [
                        { name: "_id" },
                        { name: "type" },
                        { name: "customType" },
                        { name: "visibility" },
                        { name: "description" },
                        { name: "size" }
                    ]
                }),
                isc.DynamicForm.create({
                    ID: "resourceManageForm",
                    dataSource: resourceManageDS,
                    useAllDataSourceFields: true,
                    fields: [
                        { name: "type", redrawOnChange: true },
                        { name: "customType", showIf: "form.getValue('type') == 'CUSTOM_TEXT' || form.getValue('type') == 'CUSTOM_BINARY'", required: true },
                        { name: "preview", accept: "image/png", 
                            validators: [
                                { type: "custom", 
                                    condition: function(item) {
                                        return item.file == null || item.file.type == "image/png";
                                    }, errorMessage: msgs.errImageType 
                                }, 
                                { type: "custom", 
                                    condition: function(item) {
                                        return item.file == null || item.file.size <= 50000;
                                    }, errorMessage: msgs.errPreviewSize 
                                }
                            ], 
                            changed: function(form, item, file) {
                                if (file) {
                                    item.getInfo().setSrc(file);
                                } else {
                                    item.getInfo().setSrc("[SKIN]/actions/close.png");
                                }
                            }, 
                            info: isc.Img.create({width: 22, height: 22, src: "[SKIN]/actions/close.png"}) },
                        { name: "content", 
                            validators: [
                                { type: "custom", 
                                    condition: function(item) {
                                        var type = item.form.getValue("type");
                                        var mimeType = null;
                                        if (type == "PNG")
                                            mimeType = "image/png";
                                        else if (type == "CUSTOM_BINARY")
                                            mimeType = item.form.getValue("customType");
                                        return item.file == null || mimeType == null || item.file.type == mimeType;
                                    }, errorMessage: msgs.errContentType 
                                },
                                { type: "custom", 
                                    condition: function(item) {
                                        return item.file == null || item.file.size <= 10000000;
                                    }, errorMessage: msgs.errPreviewSize 
                                }
                            ], 
                            changed: function(form, item) {
                                if (item.file) {
                                    var size = item.file.size;
                                    if (size < 1000)
                                        size = size + "B";
                                    else if (size < 1000000)
                                        size = Math.round(size / 1000) + "kB";
                                    else 
                                        size = Math.round(size / 1000000) + "MB";
                                    if (form.getValue("_id") == null) {
                                        form.setValue("_id", item.file.name);
                                    }
                                    if (form.getValue("type") == "CUSTOM_BINARY") {
                                        form.setValue("customType", item.file.type);
                                    }
                                    var info = item.getInfo();
                                    var that = this;
                                    if (info == null || info.Class !== "Label") {
                                        var info = isc.HLayout.create({
                                            width: 100,
                                            overflow: "visible",
                                            border: "1px solid lightgrey", 
                                            backgroundColor: "#ddd",
                                            members: [
                                                isc.Label.create({width: "*", height: 22, contents: "", wrap: false}),
                                                isc.ImgButton.create({width: 15, height: 15, showRollOver: false, showDown: false, src: "[SKIN]/actions/close.png", click: function() {item.file = null; that.changed(form, item);}}),
                                            ],
                                            setContents: function(text) {
                                                this.members[0].setContents(text);
                                            }
                                        });
                                        item.setInfo(info);
                                    }
                                    info.setContents(item.file.name + " (" + size + ")");
                                } else {
                                    if (form.getValue("size") == null) {
                                        item.setInfo(null);
                                    } else {
                                        item.setInfo(isc.ImgButton.create({src: "[SKIN]/actions/view.png", width: 16, height: 16, showRollOver: false, showDown: false, 
                                            click: function() {
                                                window.binFrame.location.href = "ctrl/zen2/" + form.getValue("_id") + "?sessionId=" + cm.getClientId() + "&download=true";
                                            }
                                        }));
                                    }
                                }
                            }, 
                            showIf: "form.getValue('type') == 'PNG' || form.getValue('type') == 'CUSTOM_BINARY'" },
                        { name: "textContent", type: "canvas", shouldSaveValue: true, 
                            showIf: "form.getValue('type') != null && form.getValue('type') != 'PNG' && form.getValue('type') != 'CUSTOM_BINARY'", canvasConstructor: "Button", 
                            canvasProperties: { title: "...", 
                                click: function() {
                                    require(["./textEditor"], function(editor) {
                                        var types = {
                                            "HTML": "text/html",
                                            "JS": "text/javascript", 
                                            "CSS": "text/css"
                                        };
                                        var mimeType = types[resourceManageForm.getValue("type")] || resourceManageForm.getValue("customType");
                                        editor.show(resourceManageForm.getValue("_id"), resourceManageForm.getValue("type"), mimeType, resourceManageForm.getValue("textContent"), true, function(updatedValue) {
                                            resourceManageForm.setValue("textContent", updatedValue);
                                            resourceManageForm.getField("textContent").canvas.setBorder("1px solid orange");
                                        });
                                    });
                                }
                            } 
                        }
                    ]
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { 
                                resourceManageForm.getField("textContent").canvas.setBorder(null);
                                resourceManageForm.getField("content").file = null;
                                resourceManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Resource"});
                            }
                        }),
                        isc.Button.create({
                            title: msgs.save,
                            click: function() { 
                                resourceManageForm.saveData(function(res, data, req) {
                                    if (res.status == isc.DSResponse.STATUS_SUCCESS) {
                                        resourceManageForm.getField("textContent").canvas.setBorder("1px solid green");
                                        resourceManageForm.getField("content").file = null;
                                        resourceManageForm.getField("preview").file = null;
                                    }
                                });
                            }
                        })
                    ]
                })
            ];
            resourceManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Resource"});
            return list;
        }
    };
});