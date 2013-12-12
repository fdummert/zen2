package de.zeos.zen2.security;

import java.util.Map;
import java.util.Set;

public interface Authorization {
    public Map<String, Object> getData();

    public Set<String> getChannels();
}
