package edu.wearpark.backend.exception;

import edu.wearpark.backend.ErrorCode;

public class NotFoundException extends AppException{
    public String getType() {
        return this.type;
    }
    public String getUri() {
        return this.uri;
    }
    private final String type;
    private final String uri;
    public NotFoundException(String type, String uri) {
        super(ErrorCode.RESOURCE_NOT_FOUND);
        this.type = type;
        this.uri = uri;

    }
    public NotFoundException(String type) {
        super(ErrorCode.RESOURCE_NOT_FOUND);
        this.type = type;
        this.uri = null;
    }
}
