define(["require"], function(require) {
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
                   isc.Button.create({title: this.msgs.add, click: function() {that.grid.startEditingNew();}})
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
        createCanvas: function () {
            var fileCanvas = null;
            fileCanvas = isc.Canvas.create({getInnerHTML: function() { return "<input type=\"file\" id=\"" + fileCanvas.ID + "_file\" style=\"display:none\">"; }});
            return isc.VStack.create({
                members: [
                    fileCanvas,
                    isc.Button.create({title: "test", click: function() {document.getElementById(fileCanvas.ID + "_file").click();}})
                ]
            });
        },
        showValue : function (displayValue, dataValue) {
            console.log("file values:", displayValue, dataValue);
        }
    });
});

