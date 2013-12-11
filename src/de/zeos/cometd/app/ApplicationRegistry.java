package de.zeos.cometd.app;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.stereotype.Component;

import de.zeos.db.MongoAccessor;
import de.zeos.script.ScriptEngineFacade;

@Component
public class ApplicationRegistry {
    public class Console {
        private Logger logger = LoggerFactory.getLogger(getClass());

        public void log(String msg) {
            logger.info(msg);
        }
    }

    private Map<String, String> securityHandlers = new HashMap<>();

    //@Inject
    //private Mongo mongo;
    @Inject
    private MongoDbFactory dbFactory;

    @PostConstruct
    private void temp() throws ScriptException {
        storeSecurityHandler("zen2", "function authenticate(credentials, db, digester) { " + "console.log('log from js: ' + toString(credentials));" + "if (credentials.password) {"
                + "   console.log('digested pwd: ' + digester.digest(credentials.password)); " + "}" + "var user = db.findOne({username: credentials.username}, 'user'); console.log('user: ',  user); if (user == null) throw 'auth.error';"
                + "return { channels: ['/service/foo'], data: { foo: 1 } }; " + "}");
    }

    public String getSecurityHandler(String app) {
        return this.securityHandlers.get(app);
    }

    public void storeSecurityHandler(String app, String source) throws ScriptException {
        this.securityHandlers.put(app, source);
    }

    public MongoAccessor getMongoAccessor(String app, ScriptEngineFacade facade) {
        if (app.equals("zen2")) {
            return new ScriptMongoAccessor(dbFactory, facade);
        }
        throw new UnsupportedOperationException("No db access available yet");
    }
}
