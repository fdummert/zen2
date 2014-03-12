define(["dojo/i18n!../../nls/messages"], function(msgs) {
    return {
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
        show: function(name, type, mimeType, value, loadTemplates, applyCallback) {
            var that = this;
            isc.Window.create({
                ID: "textEditorWin",
                title: name + " - " + (type != "CUSTOM_TEXT" ? type : mimeType),
                width: "100%",
                height: "100%",
                canDragReposition: true,
                canDragResize: true,
                showMinimizeButton: false,
                showMaximizeButton: true,
                dragAppearance: "target",
                items: [
                    isc.VLayout.create({
                        height: "100%",
                        width: "100%",
                        members: [
                            isc.Canvas.create({ID: "textEditorAceContainer", width: "100%", height: "*", redrawOnResize: false,
                                getInnerHTML: function() {
                                    return "<div id='texteditoraceeditor' style='width:100%;height:100%';overflow:auto></div>";
                                },
                                resizeTo: function(width, height) {
                                    var editorElem = document.getElementById("texteditoraceeditor");
                                    if (editorElem) {
                                        editorElem.style.height = height + "px";
                                        editorElem.style.width = width + "px";
                                        if (that.editor != null)
                                            that.editor.resize();
                                    }
                                    return this.Super("resizeTo", arguments);
                                },
                                containerResized: function() {
                                    var editorElem = document.getElementById("texteditoraceeditor");
                                    if (editorElem) {
                                        editorElem.style.height = textEditorAceContainer.getInnerHeight() + "px";
                                        editorElem.style.width = textEditorAceContainer.getInnerWidth() + "px";
                                    }
                                }
                            }),
                            isc.HStack.create({
                                ID: "textEditorButtons",
                                height: 15,
                                members: [
                                    isc.Button.create({height: 15, title: msgs.apply,
                                        click: function() {
                                            textEditorWin.closeClick();
                                            applyCallback(that.editor.getValue());
                                        }
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
            this.editor = ace.edit("texteditoraceeditor");
            this.editor.setTheme("ace/theme/eclipse");
            var modes = {
                "text/css": "css",
                "text/html": "html",
                "text/xml": "xml",
                "text/plain": "plain_text",
                "text/javascript": "javascript"
            };
            var mode = modes[mimeType] || "text";
            this.editor.getSession().setMode("ace/mode/" + mode);
            ace.require("ace/ext/language_tools");
            this.editor.setOptions({
                enableBasicAutocompletion: true,
                enableSnippets: true
            });
            if (loadTemplates && (type == "HTML" || type == "JS")) {
                var snippetManager = ace.require("ace/snippets").snippetManager;
                ace.config.loadModule("ace/snippets/" + modes[mimeType], function(m) {
                    if (m) {
                        snippetManager.files["ace/mode/" + modes[mimeType]] = m;
                        var snippetText = m.snippetText;
                        zen2_templateManageDS.fetchData({type: type}, function(res, data) {
                            for (var i = 0; i < data.length; i++) {
                                var name = data[i]._id;
                                var content = data[i].content;
                                snippetText += "snippet zen2:" + name + "\n" + content.replace(/^(.*)/gm, "\t$1") + "\n";
                            }
                            m.snippets = snippetManager.parseSnippetFile(snippetText);
                            snippetManager.register(m.snippets, m.scope);
                        });
                    }
                });
            }

            this.editor.setFontSize(10);
            textEditorAceContainer.containerResized();
            this.editor.setValue(value);
            this.findClass(".ace_autocomplete").style.zIndex = textEditorWin.zIndex + 100;
            
        }
    };
});