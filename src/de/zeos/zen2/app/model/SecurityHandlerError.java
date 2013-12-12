package de.zeos.zen2.app.model;

import java.util.Date;

public class SecurityHandlerError {
    private Date date;
    private String error;
    private int lineNo;
    private int colNo;

    public SecurityHandlerError() {
    }

    public SecurityHandlerError(Date date, String error, int lineNo, int colNo) {
        this.date = date;
        this.error = error;
        this.lineNo = lineNo;
        this.colNo = colNo;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getLineNo() {
        return this.lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public int getColNo() {
        return this.colNo;
    }

    public void setColNo(int colNo) {
        this.colNo = colNo;
    }
}
