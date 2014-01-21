package de.zeos.db.mongo;

public class ConverterContext<E, C> {
    private E entityObject;
    private String property;
    private C context;

    public ConverterContext(E entityObject, String property, C context) {
        this.entityObject = entityObject;
        this.property = property;
        this.context = context;
    }

    public E getEntityObject() {
        return this.entityObject;
    }

    public String getProperty() {
        return this.property;
    }

    public C getContext() {
        return this.context;
    }
}