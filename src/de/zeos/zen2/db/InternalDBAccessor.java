package de.zeos.zen2.db;

import java.util.List;

import de.zeos.zen2.app.model.Application;
import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.Entity;
import de.zeos.zen2.app.model.Enumeration;
import de.zeos.zen2.app.model.ScriptHandler;

public interface InternalDBAccessor {
    public List<Application> getApplications();

    public Application getApplication(String name);

    public List<DataView> getDataViews();

    public DataView getDataView(String name);

    public ScriptHandler getScriptHandler(Object id);

    public void updateScriptHandler(ScriptHandler scriptHandler);

    public Enumeration getEnumeration(String name);

    public Entity getEntity(String refEntityId);
}
