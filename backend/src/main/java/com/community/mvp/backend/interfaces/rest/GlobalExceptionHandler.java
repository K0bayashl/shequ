package com.community.mvp.backend.interfaces.rest;

import com.community.mvp.backend.common.api.ApiResponse;
import com.community.mvp.backend.common.error.BusinessException;
import com.community.mvp.backend.common.error.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
            .body(ApiResponse.failure(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getMessage(), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
        ConstraintViolationException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getConstraintViolations()
            .forEach(violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return ResponseEntity.badRequest()
            .body(ApiResponse.failure(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getMessage(), errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.failure(
                ErrorCode.REQUEST_BODY_ERROR,
                ErrorCode.REQUEST_BODY_ERROR.getMessage(),
                null
            ));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.failure(exception.getErrorCode(), exception.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.failure(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), null));
    }
}
