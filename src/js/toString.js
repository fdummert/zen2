    function toString(obj) {
        if (!obj)
            return "null";
        if (typeof obj === "string")
            return obj;
        if (typeof obj === "object") {
            if (obj instanceof Array) {
                var str = "[";
                for (var i = 0; i < obj.length; i++) {
                    if (str.length > 1)
                        str += ",";
                    str += toString(obj[i]);
                }
                return str + "]";
            } else {
                var str = "{";
                for (var p in obj) {
                    if (str.length > 1)
                        str += ",";
                    str += p + ":" + toString(obj[p]);
                }
                return str + "}";
            }
        }
        return "" + obj;
    }