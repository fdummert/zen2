package de.zeos.zen2.app.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "zen2.application")
public class Application {

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
