var console = {
    log: function() {
        var args = "";
        for (var i = 0; i < arguments.length; i++) {
            if (i > 0) args + " ";
            args += toString(arguments[i]);
        }
        $console.log(args);
    }
};