package de.zeos.fs;

import java.io.File;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FsController {
    @RequestMapping(value = "/{app}/{type}/{resource}.{ext}", method = RequestMethod.GET)
    @ResponseBody
    public FileSystemResource download(@PathVariable String app, @PathVariable String type, @PathVariable String resource, @PathVariable String ext, HttpServletResponse res) {
        return new FileSystemResource(new File(app + "/" + type + "/" + resource + "." + ext));
    }
}
