package com.amigoscode.interview.author.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice(
        basePackages = "com.amigoscode.interview.author"
)
public class AuthorGlobalExceptionHandler {

    @ExceptionHandler(AuthorWithBooksDeletionException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorWithBooksDeletionException(
            AuthorWithBooksDeletionException ex,
            HttpServletRequest request) {

        HttpStatus httpStatus = HttpStatus.CONFLICT;

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", httpStatus.value());
        errorResponse.put("error", httpStatus.name());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getServletPath());
        errorResponse.put("details", List.of());

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
