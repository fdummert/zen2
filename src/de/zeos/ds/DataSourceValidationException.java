package de.zeos.ds;

import java.util.Collections;
import java.util.Map;

public class DataSourceValidationException extends DataSourceException {

    private static final long serialVersionUID = 1931220585408359094L;

    private Map<String, String> errors;

    public DataSourceValidationException(String field, String error) {
        this.errors = Collections.singletonMap(field, error);
    }

    public DataSourceValidationException(Map<String, String> errors) {
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return this.errors;
    }
}
