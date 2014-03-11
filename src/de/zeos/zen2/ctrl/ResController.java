package de.zeos.zen2.ctrl;

import java.io.IOException;

import javax.inject.Inject;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import de.zeos.zen2.app.ApplicationRegistry;
import de.zeos.zen2.app.model.Resource;
import de.zeos.zen2.app.model.Resource.ResourceClass;
import de.zeos.zen2.app.model.SecurityMode;
import de.zeos.zen2.db.InternalDBAccessor;
import de.zeos.zen2.security.AuthSecurityPolicy;

@Controller
public class ResController {

    @Inject
    private BayeuxServer bayeuxServer;
    @Inject
    private ApplicationRegistry appRegistry;

    @RequestMapping(value = "/{app:(?!^fs$|^dv$)\\w+}", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> getIndex(@PathVariable String app) throws IOException {
        String path = (String) RequestContextHolder.getRequestAttributes().getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if (path.equals("/" + app)) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURI() + "/");
            return new ResponseEntity<byte[]>(null, headers, HttpStatus.FOUND);
        }
        return getResource(app, "index.html", null, null);
    }

    @RequestMapping(value = "/{app:(?!^fs$|^dv$)\\w+}/**/{id:[\\w.;=]*}", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> getResource(@PathVariable String app, @PathVariable String id, @RequestParam(value = "sessionId", required = false) String sessionId, @RequestParam(value = "download", required = false) Boolean download)
            throws IOException {
        String path = (String) RequestContextHolder.getRequestAttributes().getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        int pos = path.indexOf(app);
        if (pos != 1)
            throw new ControllerException(HttpStatus.BAD_REQUEST, "Malformed resource ID");
        path = path.substring(app.length() + 1);
        if (!path.startsWith("/"))
            throw new ControllerException(HttpStatus.BAD_REQUEST, "Malformed resource ID");
        id = path.length() == 1 ? id : path.substring(1);
        return getResource(app, id, sessionId, download != null ? download : false);
    }

    private HttpEntity<byte[]> getResource(String app, String id, String sessionId, boolean download) throws IOException {
        Resource r;
        try {
            InternalDBAccessor accessor = appRegistry.getInternalDBAccessor(app);
            r = accessor.getResource(resolve("/" + app + "/" + (download ? "download/" : "") + id, id));
        } catch (Exception e) {
            throw new ControllerException(HttpStatus.FORBIDDEN, "Invalid application");
        }
        if (r == null)
            throw new ControllerException(HttpStatus.NOT_FOUND, "Resource not accessible");
        if (r.getVisibility() != SecurityMode.PUBLIC) {
            if (sessionId == null)
                throw new ControllerException(HttpStatus.FORBIDDEN, "Resource not accessible");
            ServerSession session = this.bayeuxServer.getSession(sessionId);
            if (session == null)
                throw new ControllerException(HttpStatus.UNAUTHORIZED, "Invalid session");
            String sessionApp = (String) session.getAttribute(AuthSecurityPolicy.APP_KEY);
            if (!sessionApp.equals(app))
                throw new ControllerException(HttpStatus.FORBIDDEN, "Invalid application");
        }
        byte[] content = r.getType().getResourceClass() == ResourceClass.BINARY ? r.getContent() : r.getTextContent().getBytes();
        HttpHeaders headers = new HttpHeaders();
        if (download) {
            headers.add("Content-Disposition", "attachment; filename=" + r.getId());
        }
        headers.setContentLength(content.length);
        headers.setContentType(MediaType.valueOf(r.getContentType()));
        return new HttpEntity<byte[]>(content, headers);
    }

    private String resolve(String prefix, String id) {
        if (id.contains("."))
            return id;
        String path = (String) RequestContextHolder.getRequestAttributes().getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        String ext = path.substring(path.indexOf(prefix) + prefix.length());
        if (ext.length() > 0) {
            if (!ext.startsWith(".") || ext.contains("/"))
                throw new ControllerException(HttpStatus.BAD_REQUEST, "Malformed resource ID");
        }
        return id + ext;
    }

}
