package de.zeos.db;

import java.util.List;
import java.util.Map;

public interface DBAccessor {
    public Map<String, Object> selectSingle(Map<String, Object> query, String from);

    public List<Map<String, Object>> select(Map<String, Object> query, Integer pageFrom, Integer pageTo, String[] sortCols, String from);

    public long count(Map<String, Object> query, String from);

    public boolean insert(Map<String, Object> query, String into);

    public boolean update(Map<String, Object> query, String from);

    public boolean delete(Map<String, Object> query, String from);

}
