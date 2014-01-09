package de.zeos.zen2.data;

public class ValidationException extends Exception {

    private static final long serialVersionUID = 904673638225572977L;

    private String property;

    public ValidationException(String property, String msg) {
        super(msg);
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
