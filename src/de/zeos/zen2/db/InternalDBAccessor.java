package de.zeos.zen2.db;

import java.util.List;

import de.zeos.zen2.app.model.Application;
import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.DataViewScriptHandler;
import de.zeos.zen2.app.model.Entity;
import de.zeos.zen2.app.model.Enumeration;
import de.zeos.zen2.app.model.ScriptHandler;
import de.zeos.zen2.app.model.ScriptHandlerConsoleEntry;
import de.zeos.zen2.app.model.ScriptHandlerError;

public interface InternalDBAccessor {
    public boolean existsDB(String id);

    public List<Application> getApplications();

    public Application getApplication(String name);

    public void createApplication(Application app);

    public void deleteApplication(String name);

    public List<DataView> getDataViews();

    public DataView getDataView(String name);

    public ScriptHandler getScriptHandler(Object id);

    public void addScriptHandlerLogEntry(Object id, ScriptHandlerConsoleEntry entry);

    public void addScriptHandlerError(Object id, ScriptHandlerError error);

    public List<DataViewScriptHandler> getScriptHandlers(DataView dataView);

    public Enumeration getEnumeration(String name);

    public Entity getEntity(String refEntityId);

}
