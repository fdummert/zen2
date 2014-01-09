package de.zeos.zen2.app.model;

public class Application {

    public enum SecurityMode {
        PUBLIC, PROTECTED
    }

    private String id;
    private SecurityMode securityMode;
    private ScriptHandler securityHandler;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SecurityMode getSecurityMode() {
        return this.securityMode;
    }

    public void setSecurityMode(SecurityMode securityMode) {
        this.securityMode = securityMode;
    }

    public ScriptHandler getSecurityHandler() {
        return securityHandler;
    }

    public void setSecurityHandler(ScriptHandler securityHandler) {
        this.securityHandler = securityHandler;
    }
}
