package de.zeos.zen2.ctrl;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.zeos.zen2.app.ApplicationRegistry;
import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.data.DataViewInfo;
import de.zeos.zen2.data.FieldInfo;
import de.zeos.zen2.data.ModelInfo;
import de.zeos.zen2.db.DBAccessor;
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

    @RequestMapping(value = "/{app}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ModelInfo getDataViews(@PathVariable String app, @RequestParam(value = "sessionId") String sessionId) throws JsonProcessingException {
        ServerSession session = this.bayeuxServer.getSession(sessionId);
        if (session == null)
            throw new ControllerException(HttpStatus.UNAUTHORIZED, "Invalid session");
        String sessionApp = (String) session.getAttribute(AuthSecurityPolicy.APP_KEY);
        if (!sessionApp.equals(app))
            throw new ControllerException(HttpStatus.FORBIDDEN, "Invalid application");
        Authorization auth = (Authorization) session.getAttribute(AuthSecurityPolicy.AUTH_KEY);
        ModelInfo modelInfo = new ModelInfo();
        InternalDBAccessor accessor = appRegistry.getInternalDBAccessor(app);
        for (String view : auth.getDataViews()) {
            new DataViewInfo(modelInfo, accessor, accessor.getDataView(view));
        }
        return modelInfo;
    }

    @RequestMapping(value = "/{app}/{dataView}/{id}/{field}", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> getBinaryDataViewContent(@PathVariable String app, @PathVariable String dataView, @PathVariable String id, @PathVariable String field, @RequestParam(value = "sessionId") String sessionId) throws IOException {
        ServerSession session = this.bayeuxServer.getSession(sessionId);
        if (session == null)
            throw new ControllerException(HttpStatus.UNAUTHORIZED, "Invalid session");
        String sessionApp = (String) session.getAttribute(AuthSecurityPolicy.APP_KEY);
        if (!sessionApp.equals(app))
            throw new ControllerException(HttpStatus.FORBIDDEN, "Invalid application");
        Authorization auth = (Authorization) session.getAttribute(AuthSecurityPolicy.AUTH_KEY);
        ModelInfo modelInfo = new ModelInfo();
        if (!auth.getDataViews().contains(dataView))
            throw new ControllerException(HttpStatus.FORBIDDEN, "Invalid dataview");
        InternalDBAccessor internalAccessor = appRegistry.getInternalDBAccessor(app);
        DataViewInfo dvInfo = new DataViewInfo(modelInfo, internalAccessor, internalAccessor.getDataView(dataView));
        FieldInfo f = dvInfo.getEntity().getField(field);
        if (f == null || f.getType().getDataClass() != DataClass.BINARY)
            throw new ControllerException(HttpStatus.FORBIDDEN, "Invalid dataview field");
        DBAccessor accessor = appRegistry.getDBAccessor(app);
        Map<String, Object> result = accessor.selectSingle(Collections.singletonMap(dvInfo.getEntity().getPkFieldName(), (Object) id), dvInfo.getEntity(), true);
        byte[] content = (byte[]) result.get(field);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(content.length);
        headers.setContentType(MediaType.valueOf(f.getType().getMimeType()));
        return new HttpEntity<byte[]>(content, headers);
    }
}
