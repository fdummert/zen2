package de.zeos.zen2.db.mongo;

import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;

import de.zeos.zen2.app.model.Application;
import de.zeos.zen2.app.model.DataView;
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
    public List<DataView> getDataViews() {
        return this.operations.findAll(DataView.class);
    }

    @Override
    public DataView getDataView(String name) {
        return this.operations.findById(name, DataView.class);
    }

    @Override
    public void updateScriptHandler(ScriptHandler scriptHandler) {
        this.operations.save(scriptHandler);
    }
}
