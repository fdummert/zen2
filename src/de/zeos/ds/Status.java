package de.zeos.ds;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {

    SUCCESS(0), ERROR(-1), INVALID(-4);

    private int code;

    private Status(int code) {
        this.code = code;
    }

    @JsonValue
    public int getCode() {
        return this.code;
    }
}
