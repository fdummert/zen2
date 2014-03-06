package de.zeos.zen2.app.model;

public class Resource {
    public enum ResourceClass {
        BINARY, TEXT;
    }

    public enum Type {
        PNG("image/png", ResourceClass.BINARY), HTML("text/html", ResourceClass.TEXT), JS("text/javascript", ResourceClass.TEXT), CSS("text/css", ResourceClass.TEXT), CUSTOM_BINARY(null, ResourceClass.BINARY), CUSTOM_TEXT(null, ResourceClass.TEXT);
        private String mimeType;
        private ResourceClass resourceClass;

        private Type(String mimeType, ResourceClass resourceClass) {
            this.mimeType = mimeType;
            this.resourceClass = resourceClass;
        }

        public ResourceClass getResourceClass() {
            return this.resourceClass;
        }

        public String toMimeType() {
            return this.mimeType;
        }
    }

    private String id;
    private Type type;
    private String customType;
    private SecurityMode visibility;
    private String description;
    private String preview;
    private int size;
    private byte[] content;
    private String textContent;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getCustomType() {
        return this.customType;
    }

    public void setCustomType(String customType) {
        this.customType = customType;
    }

    public String getContentType() {
        return (this.type == Type.CUSTOM_BINARY || this.type == Type.CUSTOM_TEXT) ? this.customType : this.type.toMimeType();
    }

    public SecurityMode getVisibility() {
        return this.visibility;
    }

    public void setVisibility(SecurityMode visibility) {
        this.visibility = visibility;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreview() {
        return this.preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
}
