package edu.wearpark.backend.controller;

import edu.wearpark.backend.ErrorCode;
import edu.wearpark.backend.dto.error.ApiError;
import edu.wearpark.backend.dto.error.NotFoundError;
import edu.wearpark.backend.exception.AppException;
import edu.wearpark.backend.exception.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonErrorController {
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex) {
        ErrorCode ec = ex.getCode();
        NotFoundError body = new NotFoundError(ec.name(), ec.httpCode.value(), ec.description, ex.getType(), ex.getUri());
        return ResponseEntity.status(ec.httpCode).body(body);
    }
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiError> handleAppException(AppException ex) {
        ErrorCode ec = ex.getCode();
        ApiError body = new ApiError(ec.name(), ec.httpCode.value(), ec.description);
        return ResponseEntity.status(ec.httpCode).body(body);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleAny(Exception ex) {
        ErrorCode ec = ErrorCode.INTERNAL_ERROR;
        ApiError body = new ApiError(ec.name(), ec.httpCode.value(), ec.description);
        return ResponseEntity.status(ec.httpCode).body(body);
    }
}
