package de.zeos.fs;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FsController {

    @Inject
    private ServletContext servletContext;

    @RequestMapping(value = "/{app}", method = RequestMethod.GET)
    @ResponseBody
    public Resource getIndex(@PathVariable String app, HttpServletResponse res) throws IOException {
        return getFile(app + "/index.html", res);
    }

    @RequestMapping(value = "/{app}/{type}/{resource}.{ext}", method = RequestMethod.GET)
    @ResponseBody
    public Resource download(@PathVariable String app, @PathVariable String type, @PathVariable String resource, @PathVariable String ext, HttpServletResponse res) throws IOException {
        return getFile(app + "/" + type + "/" + resource + "." + ext, res);
    }

    private Resource getFile(String file, HttpServletResponse res) throws IOException {
        InputStream stream = this.servletContext.getResourceAsStream("/WEB-INF/apps/" + file);
        if (stream == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        byte[] content = StreamUtils.copyToByteArray(stream);
        return new ByteArrayResource(content);

    }
}
