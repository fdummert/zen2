package de.zeos.zen2.db;

import java.util.EventListener;

public interface DBListener extends EventListener {
    public String getEntityName();

    public void notify(DBEvent event);
}
