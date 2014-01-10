package de.zeos.zen2.db;

import de.zeos.script.ScriptEngineFacade;

public interface DBAccessorFactory {
    public InternalDBAccessor createInternalDBAccessor(String app);

    public DBAccessor createDBAccessor(String app);

    public DBAccessor createScriptableDBAccessor(String app, ScriptEngineFacade facade);
}
