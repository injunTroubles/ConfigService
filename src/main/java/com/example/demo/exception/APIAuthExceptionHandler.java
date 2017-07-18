package com.example.demo.exception;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by rvann on 7/17/17.
 */
public class APIAuthExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    @Override
    public void commence(HttpServletRequest req,
                         HttpServletResponse response, AuthenticationException ex)
            throws IOException, ServletException {

        handleException(req, response, ex);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        handleException(request, response, accessDeniedException);
    }

    private void handleException(HttpServletRequest req,
                                 HttpServletResponse response,
                                 Exception ex) throws IOException, ServletException {
        APIError error = new APIError(ex, req);
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(error.getStatusCode());
        response.getOutputStream().print(error.toJson());
    }
}
