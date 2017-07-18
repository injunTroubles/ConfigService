package com.example.demo.exception;

import org.springframework.http.HttpStatus;

/**
 * Created by rvann on 7/17/17.
 */
public class StatusResolver {

    public static HttpStatus fromException(Exception e) {
        HttpStatus status;
        String simpleName = e.getClass().getSimpleName();

        switch (simpleName) {
            case "BadCredentialsException":
                status = HttpStatus.UNAUTHORIZED;
                break;
            case "AccessDeniedException":
                status = HttpStatus.FORBIDDEN;
                break;
            case "NoSuchResourceException":
                status = HttpStatus.NOT_FOUND;
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
        }

        return status;
    }
}