function ServiceException(err) {
    this.toString = function() {
        return "#java.lang.IllegalStateException:" + err + "#";
    };
}

function ValidationException(prop, err) {
    this.toString = function() {
        return "#de.zeos.zen2.data.ValidationException:" + prop + "," + err + "#"; 
    };
}