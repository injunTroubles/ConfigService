package com.example.demo.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created by rvann on 7/17/17.
 */
public class APIError {

    private Date timestamp;
    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    private int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
    private String error;
    private String message;
    private String path;

    public APIError(Exception exception, HttpServletRequest request) {
        this.timestamp = new Date();
        this.status = StatusResolver.fromException(exception);
        this.statusCode = status.value();
        this.error = exception.getClass().getName();
        this.message = exception.getMessage();
        this.path = request.getRequestURI();
    }

    public APIError(Exception exception, HttpStatus status, HttpServletRequest request) {
        this.timestamp = new Date();
        this.status = status;
        this.statusCode = status.value();
        this.error = exception.getClass().getName();
        this.message = exception.getMessage();
        this.path = request.getRequestURI();
    }

    public APIError(Exception exception, HttpStatus status, String requestURI) {
        this.timestamp = new Date();
        this.status = status;
        this.statusCode = status.value();
        this.error = exception.getClass().getName();
        this.message = exception.getMessage();
        this.path = requestURI;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
