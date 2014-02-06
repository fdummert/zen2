package de.zeos.zen2.security;

import java.util.Map;

import de.zeos.zen2.db.ScriptableDBAccessor;

public interface Authenticator {
    public Map<String, Object> authenticate(Map<String, Object> credentials, ScriptableDBAccessor db, Digester digester);
}
