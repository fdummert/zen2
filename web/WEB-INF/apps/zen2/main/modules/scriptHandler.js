define(["dojo/i18n!../../nls/messages"], function(msgs) {
    return {
        origColor: null,
        editor: null,
        findClass: function(name) {
            for (var i = 0; i < document.styleSheets.length; i++) {
                var classes = document.styleSheets[i].cssRules;
                for(var j = 0; j < classes.length; j++) {
                    if (classes[j].selectorText === name) {
                        return classes[j];
                    }            
                }        
            }        
            return null;             
        },
        show: function(type, scriptHandler, def, applyCallback, saveCallback) {
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
                                                    { name: "date", type: "datetime", width: 110 },
                                                    { name: "line", type: "text" }
                                                ] 
                                            }),
                                            isc.Button.create({ID: "scriptConsoleClear", height: 15, title: msgs.clear, disabled: true,
                                                click: function() {
                                                    scriptHandler.consoleEntries = [];
                                                    zen2_scriptHandlerConsoleUpdateDS.updateData(scriptHandler, function(res, data) {
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
                                                    { name: "lineNo", width: 40 },
                                                    { name: "colNo", width: 40 },
                                                    { name: "error" }
                                                ],
                                                recordClick: function(viewer, record) {
                                                    if (record.lineNo >= 0 && record.colNo >= 0) {
                                                        var arr = that.editor.getSession().getAnnotations();
                                                        arr.push({
                                                            row: record.lineNo - 1,
                                                            column: record.colNo,
                                                            text: record.error,
                                                            type: "error"
                                                        });
                                                        that.editor.getSession().setAnnotations(arr);
                                                    }
                                                }
                                            }),
                                            isc.Button.create({ID: "scriptErrorClear", height: 15, title: msgs.clear, disabled: true,
                                                click: function() {
                                                    scriptHandler.errors = [];
                                                    zen2_scriptHandlerErrorUpdateDS.updateData(scriptHandler, function(res, data) {
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
            ace.require("ace/ext/language_tools");
            this.editor.setOptions({
                enableBasicAutocompletion: true,
                enableSnippets: true
            });
            this.findClass(".ace_editor.ace_autocomplete").style.zIndex = scriptHandlerWin.zIndex + 100;
            this.origColor = this.editor.renderer.content.style.backgroundColor;
            aceContainer.containerResized();
            
            if (applyCallback) {
                scriptHandlerSourceButtons.addMember(
                    isc.Button.create({height: 15, title: msgs.apply,
                        click: function() {
                            if (scriptHandler == null)
                                scriptHandler = { };
                            scriptHandler.source = that.editor.getValue();
                            scriptHandlerWin.closeClick();
                            applyCallback(scriptHandler);
                        }
                    })
                );
            }
            if (scriptHandler != null) {
                if (saveCallback && scriptHandler._id != null) {
                    scriptHandlerSourceButtons.addMember(
                        isc.Button.create({height: 15, title: msgs.save,
                            click: function() {
                                scriptHandler.source = that.editor.getValue();
                                zen2_scriptHandlerSourceUpdateDS.updateData(scriptHandler, function(res, data) {
                                    scriptHandler = data[0];
                                    that.update(scriptHandler);
                                    saveCallback(scriptHandler);
                                });
                            }
                        })
                    );
                }
                this.editor.setValue(scriptHandler.source);
                scriptConsoleClear.setDisabled(false);
                scriptErrorClear.setDisabled(false);
                scriptConsole.setData(scriptHandler.consoleEntries);
                scriptErrors.setData(scriptHandler.errors);
                this.update(scriptHandler);
            }
            if (scriptHandler == null || scriptHandler.source == null && def != null) {
                this.editor.setValue(def);
                var pos = this.editor.find("$");
                if (pos != null) {
                    this.editor.replace("");
                    this.editor.moveCursorTo(pos.start);
                }
            }
        },
        update: function(scriptHandler) {
            this.editor.renderer.content.style.backgroundColor = (scriptHandler.valid === false ? "orange" : this.origColor);
        }
    };
});