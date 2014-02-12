package de.zeos.zen2.ctrl;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionAdvice {

    @ExceptionHandler(value = ControllerException.class)
    public void handleControllerException(HttpServletResponse res, ControllerException e) throws IOException {
        res.sendError(e.getStatus().value(), e.getMessage());
    }
}
