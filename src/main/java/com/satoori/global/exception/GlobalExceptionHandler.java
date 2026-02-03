package com.satoori.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateDialectException.class)
    public ResponseEntity<String> handleDuplicateDialect(DuplicateDialectException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409 Conflict
                .body(e.getMessage()); // 예외 메세지 그대로 응답
    }
}
