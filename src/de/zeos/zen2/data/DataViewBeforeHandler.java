package de.zeos.zen2.data;

import java.util.Map;

import de.zeos.zen2.db.DBAccessor;

public interface DataViewBeforeHandler {
    Object process(Map<String, Object> data, DBAccessor db);
}
