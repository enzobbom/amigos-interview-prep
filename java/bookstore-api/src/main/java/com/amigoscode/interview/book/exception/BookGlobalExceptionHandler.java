package com.amigoscode.interview.book.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice(
        basePackages = "com.amigoscode.interview.book"
)
public class BookGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleRequestBodyValidationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        List<Map<String, String>> fieldErrors = ex.getFieldErrors()
                .stream()
                .map(e -> {
                    Map<String, String> error = new LinkedHashMap<>();
                    error.put("field", e.getField());
                    error.put("detail", e.getDefaultMessage() == null ? "invalid value" : e.getDefaultMessage());
                    return error;
                })
                .toList();

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", httpStatus.value());
        errorResponse.put("error", httpStatus.name());
        errorResponse.put("message", "");
        errorResponse.put("path", request.getServletPath());
        errorResponse.put("details", fieldErrors);

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
