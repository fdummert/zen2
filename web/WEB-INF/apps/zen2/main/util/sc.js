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
    
    
    isc.ClassFactory.defineClass("GridEditorItem", "CanvasItem");
    isc.GridEditorItem.addProperties({
       height:"*", width:"*",
       rowSpan:"*", colSpan:"*",
       endRow:true, startRow:true,

       // this is going to be an editable data item
       shouldSaveValue:true,

       // Override createCanvas to create the ListGrid with the user can use to set the value.
       createCanvas : function () {

           var gridDS = isc.DS.get(this.gridDataSource);

           return isc.ListGrid.create({
               autoDraw:false,

               // fill the space the form allocates to the item
               leaveScrollbarGaps:false,

               // dataSource and fields to use, provided to a listGridItem as
               // listGridItem.gridDataSource and optional gridFields
               dataSource:gridDS,
               fields:this.gridFields,
               sortField:this.gridSortField,

               // the record being edited is assumed to have a set of subrecords
               data:this.getValue(),
               canEdit:true,
               saveLocally:true,
               modalEditing:true,

               // update form when data changes
               cellChanged : function () {
                   this.canvasItem.saveValue(this.data);
                   if (this.canvasItem.gridSortField != null) {
                        this.sort(this.canvasItem.gridSortField);
                   }
               }
           });
       },

       // implement showValue to update the ListGrid data
       // Note that in this case we care about the underlying data value - an array of records
       showValue : function (displayValue, dataValue) {
           if (this.canvas == null) return;
           this.canvas.setData(dataValue);
       }
    });
});

