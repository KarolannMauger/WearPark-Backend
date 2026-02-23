package edu.wearpark.backend.controller;

import edu.wearpark.backend.ErrorCode;
import edu.wearpark.backend.dto.error.ApiError;
import edu.wearpark.backend.dto.error.NotFoundError;
import edu.wearpark.backend.exception.AppException;
import edu.wearpark.backend.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
@RequiredArgsConstructor
public class CommonErrorController {
    private final Logger log;
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex) {
        log.error(ex.getCode().toString());
        ErrorCode ec = ex.getCode();
        NotFoundError body = new NotFoundError(ec.name(), ec.httpCode.value(), ec.description, ex.getType(), ex.getUri());
        return ResponseEntity.status(ec.httpCode).body(body);
    }
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiError> handleAppException(AppException ex) {
        log.error(ex.getCode().toString());
        ErrorCode ec = ex.getCode();
        ApiError body = new ApiError(ec.name(), ec.httpCode.value(), ec.description);
        return ResponseEntity.status(ec.httpCode).body(body);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleAny(Exception ex) {
        log.error(Arrays.toString(ex.getStackTrace()));
        ErrorCode ec = ErrorCode.INTERNAL_ERROR;
        ApiError body = new ApiError(ec.name(), ec.httpCode.value(), ec.description);
        return ResponseEntity.status(ec.httpCode).body(body);
    }
}
