package edu.wearpark.backend.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;

public class NotFoundError extends ApiError {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String uri;
    public NotFoundError(String code, int httpCode, String description, String type) {
        super(code, httpCode, description);
        this.type = type;
        this.uri  = null;
    }
    public NotFoundError(String code, int httpCode, String description, String type, String uri) {
        super(code, httpCode, description);
        this.type = type;
        this.uri  = uri;
    }
}
