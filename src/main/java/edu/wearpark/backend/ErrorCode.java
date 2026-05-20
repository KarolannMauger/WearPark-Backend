package edu.wearpark.backend;

import org.springframework.http.HttpStatusCode;

import static org.springframework.http.HttpStatus.*;

public enum ErrorCode {
    ACCOUNT_DELETED(FORBIDDEN, "Account has been deleted"),
    INTERNAL_ERROR(INTERNAL_SERVER_ERROR, "Internal server error"),
    BAD_REQUEST_BODY(BAD_REQUEST, "Request body is incorrectly formed"),
    RESOURCE_NOT_FOUND(NOT_FOUND, "Resource not found"),
    LOCKED_OUT(LOCKED, "User is locked out"),
    WRONG_PASSWORD(FORBIDDEN, "Password does not match"),
    EMAIL_CONFLICT(CONFLICT, "Email already used");

    public final HttpStatusCode httpCode;
    public final String description;
    ErrorCode(HttpStatusCode httpCode, String description) {
        this.httpCode = httpCode;
        this.description = description;
    }

}
