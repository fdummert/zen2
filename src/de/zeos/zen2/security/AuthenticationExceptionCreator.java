package de.zeos.zen2.security;

public class AuthenticationExceptionCreator {
    public AuthenticationException create() {
        return new AuthenticationException();
    }

    public AuthenticationException create(String msg) {
        return new AuthenticationException(msg);
    }
}
