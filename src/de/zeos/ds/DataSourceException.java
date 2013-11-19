package de.zeos.ds;

public class DataSourceException extends Exception {

    private static final long serialVersionUID = -750493322870622475L;

    protected DataSourceException() {
    }

    public DataSourceException(String msg) {
        super(msg);
    }
}
