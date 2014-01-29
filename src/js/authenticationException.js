function AuthenticationException() {
    if (arguments.length == 0)
        this.msg = "#de.zeos.zen2.security.AuthenticationException:#";
    else
        this.msg = "#de.zeos.zen2.security.AuthenticationException:" + arguments[0] + "#";
    this.toString = function() {
        return this.msg;
    };
}