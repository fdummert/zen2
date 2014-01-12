package de.zeos.zen2.db.mongo;

import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;

import de.zeos.zen2.app.model.Application;
import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.Entity;
import de.zeos.zen2.app.model.Enumeration;
import de.zeos.zen2.app.model.ScriptHandler;
import de.zeos.zen2.db.InternalDBAccessor;

public class MongoInternalDBAccessor implements InternalDBAccessor {

    private MongoOperations operations;

    public MongoInternalDBAccessor(MongoOperations operations) {
        this.operations = operations;
    }

    @Override
    public List<Application> getApplications() {
        return this.operations.findAll(Application.class);
    }

    @Override
    public Application getApplication(String name) {
        return this.operations.findById(name, Application.class);
    }

    @Override
    public List<DataView> getDataViews() {
        return this.operations.findAll(DataView.class);
    }

    @Override
    public DataView getDataView(String name) {
        return this.operations.findById(name, DataView.class);
    }

    @Override
    public Enumeration getEnumeration(String name) {
        return this.operations.findById(name, Enumeration.class);
    }

    @Override
    public Entity getEntity(String name) {
        return this.operations.findById(name, Entity.class);
    }

    @Override
    public void updateScriptHandler(ScriptHandler scriptHandler) {
        this.operations.save(scriptHandler);
    }

    @Override
    public ScriptHandler getScriptHandler(Object id) {
        return this.operations.findById(id, ScriptHandler.class);
    }
}
