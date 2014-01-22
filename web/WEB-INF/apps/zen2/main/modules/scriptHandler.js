define(["dojo/i18n!../../nls/messages"], function(msgs) {
    return {
        origColor: null,
        editor: null,
        show: function(cm, type, scriptHandler, applyCallback) {
            var that = this;
            isc.Window.create({
                ID: "scriptHandlerWin",
                title: type,
                width: "100%",
                height: "100%",
                canDragReposition: true,
                canDragResize: true,
                showMinimizeButton: false,
                dragAppearance: "target",
                items: [
                    isc.HLayout.create({
                        width: "100%",
                        members: [
                            isc.VLayout.create({
                                width: "70%",
                                showResizeBar: true, 
                                members: [
                                    isc.Canvas.create({ID: "aceContainer", width: "100%", height: "*", redrawOnResize: false,
                                        getInnerHTML: function() {
                                            return "<div id='aceeditor' style='width:100%;height:100%';overflow:auto></div>";
                                        },
                                        resizeTo: function(width, height) {
                                            var editorElem = document.getElementById("aceeditor");
                                            if (editorElem) {
                                                editorElem.style.height = height + "px";
                                                editorElem.style.width = width + "px";
                                                if (that.editor != null)
                                                    that.editor.resize();
                                            }
                                            return this.Super("resizeTo", arguments);
                                        },
                                        containerResized: function() {
                                            var editorElem = document.getElementById("aceeditor");
                                            if (editorElem) {
                                                editorElem.style.height = aceContainer.getInnerHeight() + "px";
                                                editorElem.style.width = aceContainer.getInnerWidth() + "px";
                                            }
                                        }
                                    }),
                                    isc.HStack.create({
                                        ID: "scriptHandlerSourceButtons",
                                        height: 15,
                                        members: [
                                            isc.Button.create({height: 15, title: msgs.apply,
                                                click: function() {
                                                    if (scriptHandler == null)
                                                        scriptHandler = { };
                                                    scriptHandler.source = that.editor.getValue();
                                                    scriptHandlerWin.closeClick();
                                                    applyCallback(scriptHandler);
                                                }
                                            })
                                        ]
                                    })
                                ]
                            }),
                            isc.VLayout.create({
                                width: "30%",
                                members: [
                                    isc.VLayout.create({
                                        height: "50%",
                                        showResizeBar: true,
                                        members: [
                                            isc.Label.create({height: 15, contents: msgs.console}),
                                            isc.ListGrid.create({
                                                ID: "scriptConsole",
                                                showHeader: false,
                                                height: "*",
                                                fields: [
                                                    { name: "date", type: "datetime" },
                                                    { name: "line", type: "text" }
                                                ] 
                                            }),
                                            isc.Button.create({height: 15, title: msgs.clear,
                                                click: function() {
                                                    scriptHandler.consoleEntries = [];
                                                    scriptHandlerConsoleUpdateDS.updateData(scriptHandler, function(res, data) {
                                                        scriptConsole.setData(data[0].consoleEntries);
                                                    });
                                                }
                                            })
                                        ] 
                                    }),
                                    isc.VLayout.create({
                                        height: "50%",
                                        members: [
                                            isc.Label.create({height: 15, contents: msgs.errors}),
                                            isc.ListGrid.create({
                                                ID: "scriptErrors",
                                                showHeader: false,
                                                height: "*",
                                                fields: [
                                                    { name: "date", type: "datetime", width: 110 },
                                                    { name: "lineNo", width: 60 },
                                                    { name: "colNo", width: 60 },
                                                    { name: "error" }
                                                ]
                                            }),
                                            isc.Button.create({height: 15, title: msgs.clear,
                                                click: function() {
                                                    scriptHandler.errors = [];
                                                    scriptHandlerErrorUpdateDS.updateData(scriptHandler, function(res, data) {
                                                        scriptErrors.setData(data[0].errors);
                                                    });
                                                }
                                            })
                                        ] 
                                     })
                                ]
                            })
                        ]
                    })
                ],
                closeClick: function() {
                    this.destroy();
                    return false;
                }
            }).show();
            this.editor = ace.edit("aceeditor");
            this.editor.setTheme("ace/theme/eclipse");
            this.editor.getSession().setMode("ace/mode/javascript");
            this.editor.setFontSize(10);
            this.origColor = this.editor.renderer.content.style.backgroundColor;
            aceContainer.containerResized();
            
            if (scriptHandler != null) {
                this.editor.setValue(scriptHandler.source);
                scriptConsole.setData(scriptHandler.consoleEntries);
                scriptErrors.setData(scriptHandler.errors);
                this.update(scriptHandler);
                scriptHandlerSourceButtons.addMember(
                    isc.Button.create({height: 15, title: msgs.save,
                        click: function() {
                            scriptHandler.source = that.editor.getValue();
                            applyCallback(scriptHandler);
                            scriptHandlerSourceUpdateDS.updateData(scriptHandler, function(res, data) {
                                scriptHandler = data[0];
                                that.update(scriptHandler);
                            });
                        }
                    })
                );
            }
        },
        update: function(scriptHandler) {
            this.editor.renderer.content.style.backgroundColor = (scriptHandler.valid === false ? "orange" : this.origColor);
        }
    };
});