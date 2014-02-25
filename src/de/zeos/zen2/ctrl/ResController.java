package de.zeos.zen2.ctrl;

import java.io.IOException;

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

import de.zeos.zen2.app.ApplicationRegistry;
import de.zeos.zen2.app.model.Resource.ResourceClass;
import de.zeos.zen2.app.model.SecurityMode;
import de.zeos.zen2.db.InternalDBAccessor;
import de.zeos.zen2.security.AuthSecurityPolicy;

@Controller
@RequestMapping("/res")
public class ResController {

    @Inject
    private BayeuxServer bayeuxServer;
    @Inject
    private ApplicationRegistry appRegistry;

    @RequestMapping(value = "/{app}/{sessionId}/{id}", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> getPrivate(@PathVariable String app, @PathVariable String sessionId, @PathVariable String id) throws IOException {
        ServerSession session = this.bayeuxServer.getSession(id);
        if (session == null)
            throw new ControllerException(HttpStatus.UNAUTHORIZED, "Invalid session");
        String sessionApp = (String) session.getAttribute(AuthSecurityPolicy.APP_KEY);
        if (!sessionApp.equals(app))
            throw new ControllerException(HttpStatus.FORBIDDEN, "Invalid application");
        InternalDBAccessor accessor = appRegistry.getInternalDBAccessor(app);
        de.zeos.zen2.app.model.Resource r = accessor.getResource(id);
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
            r = accessor.getResource(id);
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

}
