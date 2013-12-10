package de.zeos.cometd.security;

import java.util.Map;
import java.util.Set;

public interface Authorization {
    public Map<String, Object> getData();

    public Set<String> getChannels();
}
