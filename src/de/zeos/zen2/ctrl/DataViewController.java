package de.zeos.zen2.ctrl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.zeos.zen2.app.ApplicationRegistry;
import de.zeos.zen2.data.DataViewInfo;
import de.zeos.zen2.db.InternalDBAccessor;
import de.zeos.zen2.security.AuthSecurityPolicy;
import de.zeos.zen2.security.Authorization;

@Controller
@RequestMapping("/dv")
public class DataViewController {

    @Inject
    private BayeuxServer bayeuxServer;
    @Inject
    private ApplicationRegistry appRegistry;

    @RequestMapping(value = "/{app}/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<DataViewInfo> getDataViews(@PathVariable String app, @PathVariable String id) throws JsonProcessingException {
        ServerSession session = this.bayeuxServer.getSession(id);
        if (session == null)
            throw new IllegalStateException("Invalid session");
        String sessionApp = (String) session.getAttribute(AuthSecurityPolicy.APP_KEY);
        if (!sessionApp.equals(app))
            throw new IllegalStateException("Invalid application");
        Authorization auth = (Authorization) session.getAttribute(AuthSecurityPolicy.AUTH_KEY);
        List<DataViewInfo> views = new ArrayList<>();
        InternalDBAccessor accessor = appRegistry.getInternalDBAccessor(app);
        for (String view : auth.getDataViews()) {
            views.add(new DataViewInfo(accessor.getDataView(view)));
        }
        return views;
    }
}
