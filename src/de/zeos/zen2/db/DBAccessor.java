package de.zeos.zen2.db;

import java.util.List;
import java.util.Map;

import de.zeos.zen2.data.EntityInfo;

public interface DBAccessor {
    public void addDBListener(DBListener listener);

    public void removeDBListener(DBListener listener);

    public boolean exists(Map<String, Object> query, EntityInfo entityInfo);

    public Map<String, Object> selectSingle(Map<String, Object> query, EntityInfo entityInfo);

    public List<Map<String, Object>> select(Map<String, Object> query, Integer pageFrom, Integer pageTo, String[] sortCols, EntityInfo entityInfo);

    public long count(Map<String, Object> query, EntityInfo entityInfo);

    public Map<String, Object> insert(Map<String, Object> query, EntityInfo entityInfo);

    public Map<String, Object> update(Map<String, Object> query, boolean refetch, EntityInfo entityInfo);

    public Map<String, Object> delete(Map<String, Object> query, EntityInfo entityInfo);

}
