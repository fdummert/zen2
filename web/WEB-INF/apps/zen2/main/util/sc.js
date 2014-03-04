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
           var props = {
               autoDraw: false,
               // fill the space the form allocates to the item
               leaveScrollbarGaps: false,

               dataSource: gridDS,
               fields: this.gridFields,

               // the record being edited is assumed to have a set of subrecords
               data: this.getValue(),
               canEdit: true,
               saveLocally: true,
               modalEditing: true,

               // update form when data changes
               cellChanged : function () {
                   that.saveValue(this.data);
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

       // implement showValue to update the ListGrid data
       // Note that in this case we care about the underlying data value - an array of records
       showValue : function (displayValue, dataValue) {
           if (this.grid == null) return;
           this.grid.setData(dataValue);
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
                    isc.Button.create({title: msgs.upload, click: function() {
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
                stack.addMember(element);
            };
            this.getInfo = function() {
                return stack.getMember(1);
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
});

