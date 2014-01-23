var console = {
    log: function() {
        $console.log(toString(Array.prototype.slice.call(arguments)));
    }
};