package de.zeos.zen2.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.cometd.annotation.Listener;
import org.cometd.annotation.Service;
import org.cometd.annotation.Session;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;
import org.springframework.stereotype.Component;

import de.zeos.db.DBAccessor;
import de.zeos.script.ScriptEngineCreator;
import de.zeos.zen2.app.ApplicationRegistry;
import de.zeos.zen2.app.model.DataView;
import de.zeos.zen2.app.model.DataView.CommandMode;
import de.zeos.zen2.db.InternalDBAccessor;

@Service("dv")
@Component
public class DataViewService {

    @Session
    private ServerSession serverSession;

    @Inject
    private ScriptEngineCreator engineCreator;

    @Inject
    private ApplicationRegistry appRegistry;

    @Listener("/service/**")
    protected void receiveOnDsChannel(ServerSession remote, Mutable message) {
        String channel = message.getChannel();
        String[] parts = channel.substring(1).split("/", 5);
        if (parts.length >= 5 && parts[2].equals("dv") && parts[3].equals("req")) {
            String app = parts[1];
            String view = parts[4];
            String scope = null;
            if (parts.length > 5)
                scope = parts[5];
            process(remote, message, app, view, scope);
        }
    }

    private void process(ServerSession remote, Mutable message, String app, String dataView, String scope) {
        InternalDBAccessor internalAccessor = this.appRegistry.getInternalDBAccessor(app);
        DataView view = internalAccessor.getDataView(dataView);

        DBAccessor accessor = this.appRegistry.getDBAccessor(app);
        Map<String, Object> data = message.getDataAsMap();
        CommandMode mode = CommandMode.valueOf((String) data.get("mode"));

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("requestId", data.get("requestId"));
        try {
            if (!view.getAllowedModes().contains(mode))
                throw new IllegalStateException("errDataViewModeNotAllowed");

            @SuppressWarnings("unchecked")
            Map<String, Object> criteria = (Map<String, Object>) data.get("criteria");
            String entity = view.getEntity().getId();
            Object result = null;
            switch (mode) {
            case CREATE:
                result = accessor.insert(criteria, entity);
                break;
            case READ:
                Integer pageFrom = (Integer) data.get("pageFrom");
                Integer pageTo = (Integer) data.get("pageTo");
                String[] sorts = (String[]) data.get("sorts");
                Integer count = null;
                List<Map<String, Object>> rows = accessor.select(criteria, pageFrom, pageTo, sorts, entity);
                if (pageFrom != null || pageTo != null) {
                    count = new Long(accessor.count(criteria, entity)).intValue();
                    res.put("pageFrom", pageFrom);
                    res.put("pageTo", pageFrom + (rows == null ? 0 : rows.size() - 1));
                    res.put("count", count);
                }
                result = rows;
                break;
            case UPDATE:
                result = accessor.update(criteria, entity);
                break;
            case DELETE:
                result = accessor.delete(criteria, entity);
                break;
            }
            res.put("result", result);
        } catch (IllegalStateException e) {
            res.put("error", e.getMessage());
        } catch (Exception e) {
            res.put("error", "errDataViewGeneral");
        }
        // res.put("validationErrors", validationErrors);

        String responseChannel = "/service/" + app + "/dv/res/" + dataView;
        if (scope != null)
            responseChannel += "/" + scope;
        remote.deliver(this.serverSession, responseChannel, res, null);
    }

}
