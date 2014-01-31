package de.zeos.zen2.data;

import java.util.Map;

import de.zeos.zen2.db.ScriptableDBAccessor;

public interface DataViewAfterHandler {
    Object process(Map<String, Object> data, String scope, Object result, ScriptableDBAccessor db);
}
