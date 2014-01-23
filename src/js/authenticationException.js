function AuthenticationException() {
    if (arguments.length == 0)
        throw "#de.zeos.zen2.security.AuthenticationException:#";
    else
        throw "#de.zeos.zen2.security.AuthenticationException:" + arguments[0] + "#";
}