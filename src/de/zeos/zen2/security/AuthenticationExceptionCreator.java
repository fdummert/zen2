package de.zeos.zen2.security;

public class AuthenticationExceptionCreator {
    public AuthenticationException create(String msg) {
        return new AuthenticationException(msg);
    }
}
