package de.zeos.zen2.db.mongo;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.DB;

import de.zeos.zen2.app.model.Application;
import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.DataViewScriptHandler;
import de.zeos.zen2.app.model.Entity;
import de.zeos.zen2.app.model.Enumeration;
import de.zeos.zen2.app.model.Resource;
import de.zeos.zen2.app.model.ScriptHandler;
import de.zeos.zen2.app.model.ScriptHandlerConsoleEntry;
import de.zeos.zen2.app.model.ScriptHandlerError;
import de.zeos.zen2.db.InternalDBAccessor;

public class MongoInternalDBAccessor implements InternalDBAccessor {

    private MongoTemplate operations;
    private String bootstrapScript;

    public MongoInternalDBAccessor(MongoTemplate operations, String bootstrapScript) {
        this.operations = operations;
        this.bootstrapScript = bootstrapScript;
    }

    @Override
    public boolean existsDB(String id) {
        return operations.getDb().getMongo().getDatabaseNames().contains(id);
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
    public void createApplication(Application app) {
        DB targetDB = operations.getDb().getMongo().getDB(app.getId());
        targetDB.eval("function() { " + bootstrapScript + "}");
        targetDB.addUser(app.getId(), app.getId().toCharArray());
    }

    @Override
    public void deleteApplication(String name) {
        DB targetDB = operations.getDb().getMongo().getDB(name);
        targetDB.dropDatabase();
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
    public Resource getResource(String name) {
        return this.operations.findById(name, Resource.class);
    }

    @Override
    public Entity getEntity(String name) {
        return this.operations.findById(name, Entity.class);
    }

    @Override
    public List<Entity> getRootEntities() {
        return this.operations.find(Query.query(Criteria.where("embeddable").ne(true)), Entity.class);
    }

    @Override
    public void addScriptHandlerLogEntry(Object id, ScriptHandlerConsoleEntry entry) {
        this.operations.updateFirst(Query.query(Criteria.where("_id").is(id)), new Update().push("consoleEntries", entry), ScriptHandler.class);
    }

    @Override
    public void addScriptHandlerError(Object id, ScriptHandlerError error) {
        this.operations.updateFirst(Query.query(Criteria.where("_id").is(id)), new Update().push("errors", error).set("valid", false), ScriptHandler.class);
    }

    @Override
    public ScriptHandler getScriptHandler(Object id) {
        return this.operations.findById(id, ScriptHandler.class);
    }

    @Override
    public List<DataViewScriptHandler> getScriptHandlers(DataView dataView) {
        return this.operations.find(Query.query(Criteria.where("dataViewId.$id").is(dataView.getId())), DataViewScriptHandler.class, "zen2.scriptHandler");
    }
}
