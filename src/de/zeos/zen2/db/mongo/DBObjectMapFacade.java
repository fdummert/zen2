package de.zeos.zen2.db.mongo;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.mongodb.DBObject;

public class DBObjectMapFacade implements Map<String, Object> {
    private DBObject dbObject;

    public DBObjectMapFacade(DBObject dbObject) {
        this.dbObject = dbObject;
    }

    @Override
    public int size() {
        return dbObject.keySet().size();
    }

    @Override
    public boolean isEmpty() {
        return dbObject.keySet().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return dbObject.containsField((String) key);
    }

    @Override
    public boolean containsValue(Object value) {
        return dbObject.toMap().containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return dbObject.get((String) key);
    }

    @Override
    public Object put(String key, Object value) {
        return dbObject.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return dbObject.removeField((String) key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        dbObject.putAll(m);
    }

    @Override
    public void clear() {
        dbObject.keySet().clear();
    }

    @Override
    public Set<String> keySet() {
        return dbObject.keySet();
    }

    @Override
    public Collection<Object> values() {
        throw new IllegalStateException("Not supported");
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        throw new IllegalStateException("Not supported");
    }

}
