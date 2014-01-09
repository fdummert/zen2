package de.zeos.db;

import java.util.List;
import java.util.Map;

public interface DBAccessor {
    public boolean exists(Map<String, Object> query, String from);

    public Map<String, Object> selectSingle(Map<String, Object> query, String from, String... joins);

    public List<Map<String, Object>> select(Map<String, Object> query, Integer pageFrom, Integer pageTo, String[] sortCols, String from, String... joins);

    public long count(Map<String, Object> query, String from);

    public Map<String, Object> insert(Map<String, Object> query, String into, String... joins);

    public boolean update(Map<String, Object> query, String from);

    public boolean delete(Map<String, Object> query, String from);

}
