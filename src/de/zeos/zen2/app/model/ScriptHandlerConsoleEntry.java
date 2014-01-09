package de.zeos.zen2.app.model;

import java.util.Date;

public class ScriptHandlerConsoleEntry {
    private Date date;
    private String line;

    public ScriptHandlerConsoleEntry() {
    }

    public ScriptHandlerConsoleEntry(Date date, String line) {
        this.date = date;
        this.line = line;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }
}
