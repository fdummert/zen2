package de.zeos.db.mongo;

public class ConverterContext<S, C> {
    private S sourceObject;
    private String property;
    private int idx = -1;
    private C context;

    public ConverterContext(S sourceObject, String property, int idx, C context) {
        this.sourceObject = sourceObject;
        this.property = property;
        this.idx = idx;
        this.context = context;
    }

    public S getSourceObject() {
        return this.sourceObject;
    }

    public String getProperty() {
        return this.property;
    }

    public int getIdx() {
        return idx;
    }

    public C getContext() {
        return this.context;
    }
}