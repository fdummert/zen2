define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    return {
        create: function(cm, app) {
            var categories = {
                "image/png": { type: "PNG", cat: "binary" },
                "text/html": { type: "HTML", cat: "text" },
                "text/javascript": { type: "JS", cat: "text" }, 
                "text/css": { type: "CSS", cat: "text" }
            };
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
                        { name: "description" }
                    ]
                }),
                isc.DynamicForm.create({
                    ID: "resourceManageForm",
                    dataSource: resourceManageDS,
                    useAllDataSourceFields: true,
                    fields: [
                        { name: "type", redrawOnChange: true },
                        { name: "customType", showIf: "form.getValue('type') == 'CUSTOM_TEXT' || form.getValue('type') == 'CUSTOM_BINARY'", required: true },
                        { name: "preview", accept: "image/png", validators: [{ type: "custom", condition: function(item) {
                            return item.file == null || item.file.type == "image/png";
                        }, errorMessage: msgs.errImageType }, { type: "custom", condition: function(item) {
                            return item.file == null || item.file.size <= 50000;
                        }, errorMessage: msgs.errPreviewSize }], changed: function(form, item, file) {
                            if (file) {
                                item.getInfo().setSrc(file);
                            } else {
                                item.getInfo().setSrc("[SKIN]/actions/close.png");
                            }
                        }, info: isc.Img.create({width: 22, height: 22, src: "[SKIN]/actions/close.png"}) },
                        { name: "content", validators: [{ type: "custom", condition: function(item) {
                            var type = item.form.getValue("type");
                            var mimeType = null;
                            if (type == "PNG")
                                mimeType = "image/png";
                            else if (type == "CUSTOM_BINARY")
                                mimeType = item.form.getValue("customType");
                            return item.file == null || mimeType == null || item.file.type == mimeType;
                        }, errorMessage: msgs.errContentType }, { type: "custom", condition: function(item) {
                            return item.file == null || item.file.size <= 10000000;
                        }, errorMessage: msgs.errPreviewSize }], changed: function(form, item, file) {
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
                                item.getInfo().setContents(item.file.name + " (" + size + ")");
                            }
                        }, info: isc.Label.create({width: 200, contents: "---"}), showIf: "form.getValue('type') == 'PNG' || form.getValue('type') == 'CUSTOM_BINARY'" },
                        { name: "textContent", showIf: "form.getValue('type') != null && form.getValue('type') != 'PNG' && form.getValue('type') != 'CUSTOM_BINARY'" }
                    ]
                }),
                isc.HStack.create({
                    members: [
                        isc.Button.create({
                            title: msgs.add,
                            click: function() { resourceManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Resource"}); }
                        }),
                        isc.Button.create({
                            title: msgs.save,
                            click: function() { resourceManageForm.saveData(); }
                        })
                    ]
                })
            ];
            resourceManageForm.editNewRecord({_class: "de.zeos.zen2.app.model.Resource"});
            return list;
        }
    };
});