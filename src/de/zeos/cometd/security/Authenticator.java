package de.zeos.cometd.security;

import java.util.Map;

import de.zeos.db.MongoAccessor;

public interface Authenticator {
    public Map<String, Object> authenticate(Map<String, Object> credentials, MongoAccessor db, Digester digester);
}
