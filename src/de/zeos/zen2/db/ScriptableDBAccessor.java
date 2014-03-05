package de.zeos.zen2.db;

import java.util.List;
import java.util.Map;

public interface ScriptableDBAccessor {
    public boolean exists(Map<String, Object> query, String entityInfo);

    public Map<String, Object> selectSingle(Map<String, Object> query, String entityInfo);

    public Map<String, Object> selectSingle(Map<String, Object> query, String entityInfo, boolean includeBinary);

    public List<Object> select(Map<String, Object> query, Integer pageFrom, Integer pageTo, String[] sortCols, String entityInfo);

    public List<Object> select(Map<String, Object> query, String entityInfo);

    public long count(Map<String, Object> query, String entityInfo);

    public Map<String, Object> insert(Map<String, Object> query, String entityInfo);

    public Map<String, Object> update(Map<String, Object> query, String entityInfo);

    public Map<String, Object> delete(Map<String, Object> query, String entityInfo);
}
