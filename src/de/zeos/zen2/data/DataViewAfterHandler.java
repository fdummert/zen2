package de.zeos.zen2.data;

import java.util.Map;

import de.zeos.zen2.db.DBAccessor;

public interface DataViewAfterHandler {
    Object process(Map<String, Object> data, Object result, DBAccessor db);
}
