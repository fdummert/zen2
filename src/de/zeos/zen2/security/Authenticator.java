package de.zeos.zen2.security;

import java.util.Map;

import de.zeos.zen2.db.DBAccessor;

public interface Authenticator {
    public Map<String, Object> authenticate(Map<String, Object> credentials, DBAccessor db, Digester digester);
}
