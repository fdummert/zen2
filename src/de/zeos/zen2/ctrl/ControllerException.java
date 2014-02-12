package de.zeos.zen2.ctrl;

import org.springframework.http.HttpStatus;

public class ControllerException extends RuntimeException {

    private static final long serialVersionUID = -6195563483137009877L;

    private HttpStatus status;

    public ControllerException(HttpStatus status, String msg) {
        super(msg);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return this.status;
    }
}
