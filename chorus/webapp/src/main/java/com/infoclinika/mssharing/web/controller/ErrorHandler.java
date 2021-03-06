package com.infoclinika.mssharing.web.controller;


import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ObjectNotFoundException;
import com.infoclinika.mssharing.web.ResourceDeniedException;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private static final int ERROR_CODE = 404;
    private static final int INTERNAL_SERVER_ERROR_CODE = 500;
    private static final int NO_RESPONSE_RECEIVED_CODE = 903;
    private static final int SESSION_TIMEOUT_CODE = 904;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String XMLHTTP_REQUEST = "XMLHttpRequest";
    private static final String X_REQUESTED_WITH = "X-Requested-With";
    private static Logger LOG = Logger.getLogger(ErrorHandler.class);


    private static final List<Class> HANDLED_EXCEPTIONS = new ArrayList<Class>() {{
        add(ObjectNotFoundException.class);
        add(ResourceDeniedException.class);
        add(AccessDenied.class);
    }};


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public void handleUncaughtException(Exception ex, HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        LOG.error("Unexpected error caught", ex);
        if (isAjax(request)) {
            if (request.getUserPrincipal() != null) {
                if (isAllowed(ex)) {
                    response.sendError(SESSION_TIMEOUT_CODE, ex.getLocalizedMessage());
                    return;
                }

                response.sendError(INTERNAL_SERVER_ERROR_CODE, "" + ex.getLocalizedMessage());
            } else {
                response.sendError(NO_RESPONSE_RECEIVED_CODE, "Session timeout");
            }
        } else {
            response.sendError(INTERNAL_SERVER_ERROR_CODE, ex.getLocalizedMessage());
        }
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public void handle404Exception(Exception ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(ERROR_CODE, ex.getLocalizedMessage());
    }



    private boolean isAllowed(Exception ex) {
        for (Class clazz : HANDLED_EXCEPTIONS) {
            if (ex.getClass() == clazz) {
                return true;
            }
        }
        return false;
    }

    private boolean isAjax(HttpServletRequest request) {
        return XMLHTTP_REQUEST.equals(request.getHeader(X_REQUESTED_WITH));
    }

    public class ResourceNotFoundException extends RuntimeException {

    }
}
