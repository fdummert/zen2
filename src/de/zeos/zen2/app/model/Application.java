package de.zeos.zen2.app.model;

import java.util.ArrayList;
import java.util.List;

public class Application {

    public enum SecurityMode {
        PUBLIC, PROTECTED
    }

    private String id;
    private SecurityMode securityMode;
    private String securityHandler;
    private boolean securityHandlerValid;
    private List<SecurityHandlerError> securityHandlerErrors = new ArrayList<>();

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

    public String getSecurityHandler() {
        return this.securityHandler;
    }

    public void setSecurityHandler(String securityHandler) {
        this.securityHandler = securityHandler;
    }

    public boolean isSecurityHandlerValid() {
        return this.securityHandlerValid;
    }

    public void setSecurityHandlerValid(boolean securityHandlerValid) {
        this.securityHandlerValid = securityHandlerValid;
    }

    public List<SecurityHandlerError> getSecurityHandlerErrors() {
        return this.securityHandlerErrors;
    }

    public void setSecurityHandlerErrors(List<SecurityHandlerError> securityHandlerErrors) {
        this.securityHandlerErrors = securityHandlerErrors;
    }
}
