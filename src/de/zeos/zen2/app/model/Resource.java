package de.zeos.zen2.app.model;

public class Resource {
    public enum Type {
        PNG("image/png"), HTML("text/html"), JS("text/javascript"), CSS("text/css"), CUSTOM(null);
        private String mimeType;

        private Type(String mimeType) {
            this.mimeType = mimeType;
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
    private byte[] preview;
    private byte[] content;

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
        return this.type == Type.CUSTOM ? this.customType : this.type.toMimeType();
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

    public byte[] getPreview() {
        return this.preview;
    }

    public void setPreview(byte[] preview) {
        this.preview = preview;
    }

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
