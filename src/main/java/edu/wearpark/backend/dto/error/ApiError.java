package edu.wearpark.backend.dto.error;

import lombok.Getter;

@Getter
public class ApiError {
    String code;
    Integer httpCode;
    String description;
    public ApiError(String code, int httpCode, String description) {
        this.code = code;
        this.httpCode = httpCode;
        this.description = description;
    }
}
