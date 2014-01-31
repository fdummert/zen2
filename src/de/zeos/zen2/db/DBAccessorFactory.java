package de.zeos.zen2.db;

import java.util.Map;

import de.zeos.script.ScriptEngineFacade;
import de.zeos.zen2.data.EntityInfo;

public interface DBAccessorFactory {
    public InternalDBAccessor createInternalDBAccessor(String app);

    public DBAccessor createDBAccessor(String app);

    public ScriptableDBAccessor createScriptableDBAccessor(String app, Map<String, EntityInfo> entities, ScriptEngineFacade facade);
}
