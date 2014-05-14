define(["dojo/i18n!../../nls/messages", "require"], function(msgs, require) {
    isc.defineClass("GridEditorItem", "CanvasItem");
    isc.GridEditorItem.addProperties({
       height:"*", width:"*",
       rowSpan:"*", colSpan:"*",
       endRow:true, startRow:true,

       // this is going to be an editable data item
       shouldSaveValue:true,

       // Override createCanvas to create the ListGrid with the user can use to set the value.
       createCanvas : function () {
           var gridDS = isc.DS.get(this.gridDataSource);
           var that = this;
           var values = this._convertScalar(this.getValue());
           var props = {
               autoDraw: false,
               // fill the space the form allocates to the item
               leaveScrollbarGaps: false,

               dataSource: gridDS,
               fields: this.gridFields,

               // the record being edited is assumed to have a set of subrecords
               data: values,
               canEdit: true,
               saveLocally: true,
               modalEditing: true,

               // update form when data changes
               editComplete : function () {
                   var values = this.data;
                   if (values && that.gridScalar === true) {
                       var v = [];
                       for (var i = 0; i < values.length; i++) {
                           if (values[i].value)
                               v.push(values[i].value);
                       }
                       values = v;
                   }
                   that.saveValue(values);
                   this.resort();
               }
           };
           props = isc.addProperties({}, props, this.gridProperties);
           this.grid = isc.ListGrid.create(props);
           
           return isc.VStack.create({
               members: [
                   this.grid,
                   isc.Button.create({title: msgs.add, click: function() {that.grid.startEditingNew();}})
               ]
           });
       },
       
       _convertScalar: function(values) {
           if (values && this.gridScalar === true) {
               var v = [];
               for (var i = 0; i < values.length; i++) {
                   v.push({value: values[i]});
               }
               values = v;
           }
           return values || [];
       },

       // implement showValue to update the ListGrid data
       // Note that in this case we care about the underlying data value - an array of records
       showValue : function (displayValue, dataValue) {
           if (this.grid == null) return;
           this.grid.setData(this._convertScalar(dataValue));
       }
    });
    
    isc.defineClass("CustomFileItem", "CanvasItem");
    isc.CustomFileItem.addProperties({
        shouldSaveValue: true,
        handleFiles: function(input) {
            var file = null;
            if (input.files.length > 0)
                file = input.files[0];
            this.file = file;
            if (this.validate()) {
                var reader = new FileReader();
                var that = this;
                reader.onloadend = function() {
                    that.saveValue(reader.result);
                    if (that.changed)
                        that.changed(that.form, that, reader.result);
                };
                reader.readAsDataURL(file);
            }
        },
        createCanvas: function () {
            var that = this;
            var fileCanvas = null;
            fileCanvas = isc.Canvas.create({width: 1, height: 1, getInnerHTML: function() {
                var accept = "";
                if (that.accept)
                    accept = "accept=\"" + that.accept + "\" ";
                return "<input type=\"file\" id=\"" + fileCanvas.ID + "_file\" " + accept + "style=\"display:none\">"; 
            }});
            function handleFiles() {
                that.handleFiles(this);
            }
            var stack = isc.HStack.create({
                height: 22,
                membersMargin: 5,
                members: [
                    isc.Button.create({title: msgs.selectFile, click: function() {
                        var fileUpload = document.getElementById(fileCanvas.ID + "_file");
                        if (fileUpload._handler == null) {
                            fileUpload.addEventListener("change", handleFiles, false);
                            fileUpload._handler = true;
                        }
                        fileUpload.click();
                    }})
                ]
            });
            this.setInfo = function(element) {
                if (stack.members.length == 2) {
                    stack.removeMember(that.getInfo());
                }
                if (element != null)
                    stack.addMember(element);
            };
            this.getInfo = function() {
                if (stack.members.length == 2)
                    return stack.getMember(1);
                return null;
            };
            if (this.info) {
                this.setInfo(this.info);
            }
            return isc.VStack.create({
                members: [
                    fileCanvas,
                    stack
                ]
            });
        },
        showValue : function (displayValue) {
            if (this.changed) {
                this.changed(this.form, this, displayValue);
            }
        }
    });
    
    isc.defineClass("PKTextItem", "TextItem");
    isc.PKTextItem.addProperties({
        showIf: function(item, value, form, record) {
            item.setDisabled(record._persistent === true);
            return true;
        }
    });
});

