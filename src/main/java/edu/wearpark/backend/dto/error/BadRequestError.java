package edu.wearpark.backend.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;

public class BadRequestError extends ApiError {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String reason;
    public BadRequestError(String code, int httpCode, String description, String reason) {
        super(code, httpCode, description);
        this.reason = reason;
    }
}
