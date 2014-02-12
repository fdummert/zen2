package de.zeos.zen2.ctrl;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;

@Controller
@RequestMapping("/fs")
public class FsController {

    @Inject
    private ServletContext servletContext;

    @RequestMapping(value = "/{app}", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> getIndex(@PathVariable String app, HttpServletResponse res) throws IOException {
        return getFile(app + "/index.html", res);
    }

    @RequestMapping(value = "/{app}/**/{resource}.{ext}", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> download(@PathVariable String app, @PathVariable String resource, @PathVariable String ext, HttpServletRequest req, HttpServletResponse res) throws IOException {
        String path = (String) RequestContextHolder.getRequestAttributes().getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        return getFile(path.substring(path.indexOf(app)), res);
    }

    private HttpEntity<byte[]> getFile(String file, HttpServletResponse res) throws IOException {
        InputStream stream = this.servletContext.getResourceAsStream("/WEB-INF/apps/" + file);
        if (stream == null)
            throw new ControllerException(HttpStatus.NOT_FOUND, "Resource not found");
        byte[] content = StreamUtils.copyToByteArray(stream);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(content.length);
        headers.setContentType(ActivationMediaTypeFactory.getMediaType(file));
        return new HttpEntity<byte[]>(content, headers);

    }
}
