define(["dojo/i18n!../../nls/messages"], function(msgs) {
    return {
        show: function(cm, type, scriptHandler, cb) {
            var origColor = null;
            var editor = null;
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
                                                if (editor != null)
                                                    editor.resize();
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
                                    isc.Button.create({height: 15, title: msgs.apply,
                                        click: function() {
                                            if (scriptHandler == null)
                                                scriptHandler = { };
                                            scriptHandler.source = editor.getValue();
                                            scriptHandlerWin.closeClick();
                                            cb(scriptHandler);
                                        }
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
                                            isc.Button.create({height: 15, title: msgs.clear})
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
                                            isc.Button.create({height: 15, title: msgs.clear})
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
            editor = ace.edit("aceeditor");
            editor.setTheme("ace/theme/eclipse");
            editor.getSession().setMode("ace/mode/javascript");
            origColor = editor.renderer.content.style.backgroundColor;
            aceContainer.containerResized();
            
            if (scriptHandler != null) {
                editor.setValue(scriptHandler.source);
                scriptConsole.setData(scriptHandler.consoleEntries);
                scriptErrors.setData(scriptHandler.errors);
                if (scriptHandler.valid === false)
                    editor.renderer.content.style.backgroundColor = "orange";
            }
        }
    };
});