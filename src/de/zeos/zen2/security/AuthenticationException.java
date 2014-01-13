package de.zeos.zen2.security;

public class AuthenticationException extends Exception {

    private static final long serialVersionUID = 4870246484955525526L;

    public AuthenticationException() {
    }

    public AuthenticationException(String msg) {
        super(msg);
    }
}
