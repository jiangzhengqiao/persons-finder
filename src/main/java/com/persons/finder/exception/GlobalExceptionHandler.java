package com.persons.finder.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. business
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Business Error", ex.getMessage(), request);
    }

    // 2. security
    @ExceptionHandler(SecurityValidationException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityValidationException ex, WebRequest request) {
        // 使用 403 表示禁止执行该非法操作
        return buildResponse(HttpStatus.FORBIDDEN, "Security Policy Violation", ex.getMessage(), request);
    }

    // 3. parameter
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", errorMessage, request);
    }

    // 4. 404 Not Found
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        return buildResponse(ex.getStatus(), "Request Error", ex.getReason(), request);
    }

    // 5. unknown
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
//        ex.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred. Please contact support.", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorType, String message, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorType,
                message,
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, status);
    }
}
