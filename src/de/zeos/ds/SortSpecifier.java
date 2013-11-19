package de.zeos.ds;

public class SortSpecifier {
    public static enum Direction {
        ascending, descending
    }

    private String property;
    private SortSpecifier.Direction direction;

    public String getProperty() {
        return this.property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public SortSpecifier.Direction getDirection() {
        return this.direction;
    }

    public void setDirection(SortSpecifier.Direction direction) {
        this.direction = direction;
    }
}