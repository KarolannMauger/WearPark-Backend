package edu.wearpark.backend.exception;

import edu.wearpark.backend.ErrorCode;

public class AppException extends RuntimeException{
    final private ErrorCode code;
    public ErrorCode getCode() {
        return code;
    }
    public AppException(ErrorCode code) {
        this.code = code;
    }
}
