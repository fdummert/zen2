package de.zeos.zen2.db;

import java.util.List;

import de.zeos.zen2.app.model.Application;
import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.ScriptHandler;

public interface InternalDBAccessor {
    public List<Application> getApplications();

    public List<DataView> getDataViews();

    public DataView getDataView(String name);

    public void updateScriptHandler(ScriptHandler scriptHandler);
}
