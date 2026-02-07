package edu.wearpark.backend.exception;

import edu.wearpark.backend.ErrorCode;

public class BadRequestException extends AppException{
    final private String reason;
    public BadRequestException(String reason) {
        super(ErrorCode.BAD_REQUEST_BODY);
        this.reason = reason;
    }
    public String getReason() {
        return reason;
    }
}
