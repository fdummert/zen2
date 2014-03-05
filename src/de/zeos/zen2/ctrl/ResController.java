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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;

import de.zeos.zen2.app.ApplicationRegistry;
import de.zeos.zen2.app.model.DataClass;
import de.zeos.zen2.app.model.Resource.ResourceClass;
import de.zeos.zen2.app.model.SecurityMode;
import de.zeos.zen2.data.DataViewInfo;
import de.zeos.zen2.data.FieldInfo;
import de.zeos.zen2.data.ModelInfo;
import de.zeos.zen2.db.DBAccessor;
import de.zeos.zen2.db.InternalDBAccessor;
import de.zeos.zen2.security.AuthSecurityPolicy;
import de.zeos.zen2.security.Authorization;

@Controller
@RequestMapping("/res")
public class ResController {

    @Inject
    private BayeuxServer bayeuxServer;
    @Inject
    private ApplicationRegistry appRegistry;

    @RequestMapping(value = "/{app}/{sessionId}/{dataView}/{id}/{field}", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> getDataView(@PathVariable String app, @PathVariable String sessionId, @PathVariable String dataView, @PathVariable String id, @PathVariable String field) throws IOException {
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

    @RequestMapping(value = "/{app}/{sessionId}/{id}", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> getPrivate(@PathVariable String app, @PathVariable String sessionId, @PathVariable String id) throws IOException {
        ServerSession session = this.bayeuxServer.getSession(sessionId);
        if (session == null)
            throw new ControllerException(HttpStatus.UNAUTHORIZED, "Invalid session");
        String sessionApp = (String) session.getAttribute(AuthSecurityPolicy.APP_KEY);
        if (!sessionApp.equals(app))
            throw new ControllerException(HttpStatus.FORBIDDEN, "Invalid application");
        InternalDBAccessor accessor = appRegistry.getInternalDBAccessor(app);
        de.zeos.zen2.app.model.Resource r = accessor.getResource(resolve("/" + app + "/" + sessionId + "/" + id, id));
        if (r != null) {
            byte[] content = r.getType().getResourceClass() == ResourceClass.BINARY ? r.getContent() : r.getTextContent().getBytes();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentLength(content.length);
            headers.setContentType(MediaType.valueOf(r.getContentType()));
            return new HttpEntity<byte[]>(content, headers);
        }
        throw new ControllerException(HttpStatus.NOT_FOUND, "Resource not accessible");
    }

    @RequestMapping(value = "/{app}/{id}", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> getPublic(@PathVariable String app, @PathVariable String id) throws IOException {
        de.zeos.zen2.app.model.Resource r;
        try {
            InternalDBAccessor accessor = appRegistry.getInternalDBAccessor(app);
            r = accessor.getResource(resolve("/" + app + "/" + id, id));
        } catch (Exception e) {
            throw new ControllerException(HttpStatus.FORBIDDEN, "Invalid application");
        }
        if (r == null)
            throw new ControllerException(HttpStatus.NOT_FOUND, "Resource not accessible");
        if (r.getVisibility() != SecurityMode.PUBLIC)
            throw new ControllerException(HttpStatus.FORBIDDEN, "Resource not accessible");
        byte[] content = r.getType().getResourceClass() == ResourceClass.BINARY ? r.getContent() : r.getTextContent().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(content.length);
        headers.setContentType(MediaType.valueOf(r.getContentType()));
        return new HttpEntity<byte[]>(content, headers);
    }

    private String resolve(String prefix, String id) {
        String path = (String) RequestContextHolder.getRequestAttributes().getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        String ext = path.substring(path.indexOf(prefix) + prefix.length());
        if (ext.length() > 0) {
            if (!ext.startsWith(".") || ext.contains("/"))
                throw new ControllerException(HttpStatus.BAD_REQUEST, "Malformed resource ID");
        }
        return id + ext;
    }

}
