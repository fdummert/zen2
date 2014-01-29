package de.zeos.zen2.db.mongo;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.core.io.Resource;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.util.StreamUtils;

import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

import de.zeos.script.ScriptEngineFacade;
import de.zeos.zen2.db.DBAccessor;
import de.zeos.zen2.db.DBAccessorFactory;
import de.zeos.zen2.db.InternalDBAccessor;
import de.zeos.zen2.script.ScriptMongoAccessor;

public class MongoAccessorFactory implements DBAccessorFactory {
    @Inject
    private Mongo mongo;
    private String user;
    private String password;
    private Resource bootstrapScript;
    private String loadedBootstrapScript;

    private WriteConcern writeConcern;

    @PostConstruct
    private void init() throws IOException {
        loadedBootstrapScript = StreamUtils.copyToString(bootstrapScript.getInputStream(), Charset.defaultCharset());
    }

    public void setWriteConcern(WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBootstrapScript(Resource bootstrapScript) {
        this.bootstrapScript = bootstrapScript;
    }

    @Override
    public DBAccessor createScriptableDBAccessor(String app, ScriptEngineFacade facade) {
        return new ScriptMongoAccessor(new SimpleMongoDbFactory(mongo, app, new UserCredentials(app, app)), facade);
    }

    @Override
    public DBAccessor createDBAccessor(String app) {
        SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo, app, new UserCredentials(app, app));
        factory.setWriteConcern(this.writeConcern);
        return new MongoAccessor(factory);
    }

    @Override
    public InternalDBAccessor createInternalDBAccessor(String app) {
        MongoTemplate template = new MongoTemplate(mongo, app, new UserCredentials(user, password));
        template.setWriteConcern(writeConcern);
        return new MongoInternalDBAccessor(template, loadedBootstrapScript);
    }
}
