package de.zeos.ds;

import javax.inject.Inject;

import org.cometd.annotation.Session;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import de.zeos.cometd.security.AuthSecurityPolicy;
import de.zeos.cometd.security.Authorization;

public abstract class AbstractDataSourceService {
    @Session
    private ServerSession serverSession;
    @Inject
    private MongoOperations ops;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void receiveOnDsChannel(ServerSession remote, Mutable message) {
        String prefix = getDsChannelPrefix();
        String topic = message.getChannel();
        String[] scopes = topic.substring((prefix + "/req/").length()).split("/");
        String dsName = scopes[0];
        String responseChannel = topic.replace("/req/", "/res/");
        DataSourceRequest req = (DataSourceRequest) message.getData();
        DataSourceResponse res = new DataSourceResponse(req.getRequestId());
        try {
            Class<?> dsClass = null;
            try {
                dsClass = ClassUtils.forName(getDsPackage() + "." + StringUtils.capitalize(dsName), ClassUtils.getDefaultClassLoader());
            } catch (ClassNotFoundException | LinkageError e) {
                throw new DataSourceException("errDSNotFound");
            }
            AbstractDataSource ds = (AbstractDataSource) dsClass.getConstructor(Authorization.class, MongoOperations.class).newInstance(remote.getAttribute(AuthSecurityPolicy.AUTH_KEY), this.ops);
            if (!ds.getAllowedOperations().contains(req.getOperationType()))
                throw new DataSourceException("errDSOperationNotAllowed");

            switch (req.getOperationType()) {
            case fetch:
                ds.fetch(req, res);
                break;
            case add:
                ds.add(req, res);
                break;
            case remove:
                ds.remove(req, res);
                break;
            case update:
                ds.update(req, res);
                break;
            }
        } catch (DataSourceValidationException e) {
            res.setStatus(Status.INVALID);
            res.setErrors(e.getErrors());
        } catch (DataSourceException e) {
            res.setStatus(Status.ERROR);
            res.setData(e.getMessage());
        } catch (Exception e) {
            this.logger.error("DataSource request error", e);
            res.setStatus(Status.ERROR);
            res.setData("system.error");
        }
        res.setOperationType(req.getOperationType());
        remote.deliver(this.serverSession, responseChannel, res, null);
    }

    protected abstract String getDsChannelPrefix();

    protected abstract String getDsPackage();
}
